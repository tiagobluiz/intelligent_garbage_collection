package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Container;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.functions.ContainerStatistics;
import pt.wastemanagement.api.requesters.ContainerRequester;
import pt.wastemanagement.api.views.output.Options;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ContainerMapper  implements ContainerRequester {
    public static final String
            CONTAINER_ID_COLUMN_NAME = "container_id",
            IOT_ID_COLUMN_NAME = "iot_id",
            ACTIVE_COLUMN_NAME = "active",
            LATITUDE_COLUMN_NAME = "latitude",
            LONGITUDE_COLUMN_NAME = "longitude",
            CONTAINER_TYPE_COLUMN_NAME = "container_type",
            HEIGHT_COLUMN_NAME = "height",
            LAST_READ_DATE_COLUMN_NAME = "last_read_date",
            BATTERY_COLUMN_NAME = "battery",
            OCCUPATION_COLUMN_NAME = "occupation",
            TEMPERATURE_COLUMN_NAME = "temperature",
            COLLECT_ZONE_ID_COLUMN_NAME = "collect_zone_id",
            CONFIGURATION_ID_COLUMN_NAME = "configuration_id",
    //Container statistics
            NUMBER_OF_COLLECTS_COLUMN_NAME = "num_collects",
            NUMBER_OF_WASHES_COLUMN_NAME = "num_washes",
    //Generic columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";


    public static final Options
            GENERAL_CONTAINER_TYPE = new Options("General", "general"),
            PLASTIC_CONTAINER_TYPE = new Options("Plastic", "plastic"),
            PAPER_CONTAINER_TYPE = new Options("Paper", "paper"),
            GLASS_CONTAINER_TYPE = new Options("Glass", "glass");

    public static final List<Options> CONTAINER_TYPES =
            Arrays.asList(new Options[]{GENERAL_CONTAINER_TYPE, PLASTIC_CONTAINER_TYPE, PAPER_CONTAINER_TYPE, GLASS_CONTAINER_TYPE});


    private final String
            CREATE_CONTAINER = "{call dbo.CreateContainer(?,?,?,?,?,?,?,?)}", //iotId, latitude, longitude, height, container_type, collect_zone_id, configuration_id, container_id OUT
            UPDATE_CONTAINER_CONFIGURATION = "{call dbo.UpdateContainerConfiguration(?,?,?,?,?)}", //container_id, iotId, height, container_type, configuration_id,
            UPDATE_CONTAINER_LOCALIZATION = "{call dbo.UpdateContainerLocalization(?,?,?,?)}", //container_id, latitude, longitude, collect_zone_id
            UPDATE_CONTAINER_READS = "{call dbo.UpdateContainerReads(?,?,?,?)}", //iot_id, battery, occupation, temperature
            DEACTIVATE_CONTAINER = "{call dbo.DeactivateContainer(?)}", //container_id
            ACTIVATE_CONTAINER = "{call dbo.ActivateContainer(?)}", //container_id
            GET_COLLECT_ZONE_CONTAINERS = "SELECT * FROM dbo.GetCollectZoneContainers(?,?,?)", //page, rows, collect_zone_id
            GET_COLLECT_ZONE_ACTIVE_CONTAINERS = "SELECT * FROM dbo.GetCollectZoneActiveContainers(?,?,?)", //page, rows, collect_zone_id
            GET_CONTAINER_INFO = "SELECT * FROM dbo.GetContainerInfo(?)", //container_id
            GET_CONTAINER_BY_IOT_ID = "SELECT * FROM dbo.GetContainerByIotId(?)", //iot_id
            GET_CONTAINER_STATISTICS = "SELECT * FROM dbo.GetContainerStatistics(?)", //container_id
            GET_CONTAINERS_WITH_OCCUPATION_BETWEEN_RANGE = "{? = call dbo.GetContainersWithOccupationBetweenRange(?,?)}", //min, max
            GET_CONTAINERS_OF_ROUTE_WITH_OCCUPATION_BETWEEN_RANGE =
                    "{? = call dbo.GetContainersOfARouteWithOccupationBetweenRange(?,?,?)}", //route_id, min, max
            GET_ROUTE_CONTAINERS = "SELECT * FROM dbo.GetRouteContainers(?,?,?)", //page, rows, routeId
            GET_ROUTE_ACTIVE_CONTAINERS = "SELECT * FROM dbo.GetRouteActiveContainers(?,?,?)"; //page, rows, routeId

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(ContainerMapper.class);

    public ContainerMapper (Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new container
     * @param iotId identifier of the embedded system
     * @param latitude latitude coordinates of the container final location
     * @param longitude longitude coordinates of the container final location
     * @param collectZoneId identifier of the collect zone where this container will, initially, be inserted
     * @param configurationId identifier of the configuration to associate to the container
     * @return the identifier of the new container
     * @throws SQLInvalidDependencyException if one of the identifiers (containerId or configurationId)
     *                                      leads to an invalid entry on the database
     * @throws SQLException
     */
    public int createContainer (String iotId, float latitude, float longitude, int height, String containerType,
                                int collectZoneId, int configurationId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_CONTAINER)){
            st.setString(1, iotId);
            st.setFloat(2, latitude);
            st.setFloat(3, longitude);
            st.setInt(4, height);
            st.setString(5, containerType);
            st.setInt(6, collectZoneId);
            st.setInt(7, configurationId);
            st.registerOutParameter(8, Types.INTEGER);
            st.execute();
            return st.getInt(8);
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.createContainer()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the collect zone or configuration is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates the information of a container. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param containerId identifier of the container to be updated
     * @param iotId new identifier of the embedded system
     * @param height new height of the container
     * @param configurationId new identifier of the configuration to associate to the container
     * @throws SQLInvalidDependencyException if the configuration leads to an invalid entry on the database
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateContainerConfiguration (int containerId, String iotId, int height, String containerType, int configurationId)
            throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_CONTAINER_CONFIGURATION)) {
            st.setInt(1, containerId);
            st.setString(2, iotId);
            st.setInt(3, height);
            st.setString(4, containerType);
            st.setInt(5, configurationId);
            st.executeUpdate();
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.updateContainerConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the configuration is invalid");
            else if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Container table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates the localization of the container.
     * @param containerId identifier of the container to update
     * @param latitude new latitude coordinates of the container final location
     * @param longitude new longitude coordinates of the container final location
     * @param collectZoneId new identifier of the collect zone where this container will be inserted. If a new
     *                      collect zone should be created, then the value MUST be -1, and it will be inserted in
     *                      the route that the current collect zone is. If the container will be relocated to an
     *                      existent collect zone that has already one or more containers, then the localization
                            MUST be at a maximum of 10 meters of distance of the other containers.
     * @throws SQLInvalidDependencyException if the collectZone leads to an invalid or inactive entry on the database
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     */
    public void updateContainerLocalization (int containerId, float latitude, float longitude, int collectZoneId)
            throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_CONTAINER_LOCALIZATION)) {
            st.setInt(1, containerId);
            st.setFloat(2, latitude);
            st.setFloat(3, longitude);
            st.setInt(4, collectZoneId);
            st.executeUpdate();
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.updateContainerLocalization()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the collect zone is invalid");
            else if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Container table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates container reads. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param iotId identifier of the container to be updated
     * @param battery new value of the battery
     * @param occupation new value of the occupation
     * @param temperature new value of the temperature
     * @throws SQLNonExistentEntryException if the entry with the identified by (@iotId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateContainerReads (String iotId, short battery, short occupation, short temperature) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_CONTAINER_READS)) {
            st.setString(1, iotId);
            st.setShort(2, battery);
            st.setShort(3, occupation);
            st.setShort(4, temperature);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.updateContainerReads()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Container table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deactivates a container
     * @param containerId identifier of the container to be deactivated
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deactivateContainer (int containerId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DEACTIVATE_CONTAINER)) {
            st.setInt(1, containerId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.deactivateContainer()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to deactivate a non existent entry on Container table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Activates a container
     * @param containerId identifier of the container to be deactivated
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void activateContainer (int containerId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(ACTIVATE_CONTAINER)) {
            st.setInt(1, containerId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.activateContainer()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to activate a non existent entry on Container table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns the containers of a collect zone
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param collectZoneId identifier of the collect zone to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements representing all the containers on the respective collect zone
     * @throws SQLException
     */
    public PaginatedList<Container> getCollectZoneContainers(int pageNumber, int rowsPerPage, int collectZoneId,
                                                                            boolean showInactive) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Container> containers = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(showInactive? GET_COLLECT_ZONE_CONTAINERS : GET_COLLECT_ZONE_ACTIVE_CONTAINERS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, collectZoneId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, containers);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do {
                Timestamp lastReadDate = rs.getTimestamp(LAST_READ_DATE_COLUMN_NAME);
                containers.add(new Container(rs.getInt(CONTAINER_ID_COLUMN_NAME), rs.getString(IOT_ID_COLUMN_NAME),
                        rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                        rs.getShort(HEIGHT_COLUMN_NAME), rs.getString(CONTAINER_TYPE_COLUMN_NAME), lastReadDate == null ? null : lastReadDate.toLocalDateTime(),
                        rs.getShort(TEMPERATURE_COLUMN_NAME), rs.getShort(OCCUPATION_COLUMN_NAME), rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME),
                        rs.getInt(CONFIGURATION_ID_COLUMN_NAME), rs.getShort(BATTERY_COLUMN_NAME)
                ));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, containers);
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getCollectZoneContainers()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @ContainerMapper.getCollectZoneContainers()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @ContainerMapper.getCollectZoneContainers() " +
                        "because it was null");
            }
        }
    }

    /**
     * Returns all the containers of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements representing all the containers of a route
     * @throws SQLException
     */
    public PaginatedList<Container> getRouteContainers (int pageNumber, int rowsPerPage, int routeId, boolean showInactive) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Container> containers = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(showInactive ? GET_ROUTE_CONTAINERS : GET_ROUTE_ACTIVE_CONTAINERS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, routeId);
            rs = st.executeQuery();
            if (!rs.next()) return new PaginatedList<>(totalEntries, containers);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                Timestamp lastReadDate = rs.getTimestamp(LAST_READ_DATE_COLUMN_NAME);
                containers.add(new Container(rs.getInt(CONTAINER_ID_COLUMN_NAME), rs.getString(IOT_ID_COLUMN_NAME),
                        rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                        rs.getShort(HEIGHT_COLUMN_NAME), rs.getString(CONTAINER_TYPE_COLUMN_NAME), lastReadDate == null ? null : lastReadDate.toLocalDateTime(),
                        rs.getShort(TEMPERATURE_COLUMN_NAME), rs.getShort(OCCUPATION_COLUMN_NAME), rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME),
                        rs.getInt(CONFIGURATION_ID_COLUMN_NAME), rs.getShort(BATTERY_COLUMN_NAME)
                ));
            }while (rs.next());
            return new PaginatedList<>(totalEntries, containers);
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getRouteContainers()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("Couldn't close the result set on @ContainerMapper.getRouteContainers()");
            } catch (NullPointerException npe) {
                log.info("Couldn't close the result set on @ContainerMapper.getRouteContainers() " +
                        "because it was null");
            }
        }
    }

    /**
     * Returns all the data available for a specific container
     * @param containerId identifier of the container
     * @return an instance of GetContainerInfo
     * @throws SQLException
     */
    public Container getContainerInfo(int containerId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_INFO)) {
            st.setInt(1, containerId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent container");
                return null;
            }
            Timestamp lastReadDate = rs.getTimestamp(LAST_READ_DATE_COLUMN_NAME);
            return new Container(rs.getInt(CONTAINER_ID_COLUMN_NAME), rs.getString(IOT_ID_COLUMN_NAME),
                    rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                    rs.getShort(HEIGHT_COLUMN_NAME), rs.getString(CONTAINER_TYPE_COLUMN_NAME), lastReadDate == null ? null : lastReadDate.toLocalDateTime(),
                    rs.getShort(TEMPERATURE_COLUMN_NAME), rs.getShort(OCCUPATION_COLUMN_NAME), rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME),
                    rs.getInt(CONFIGURATION_ID_COLUMN_NAME), rs.getShort(BATTERY_COLUMN_NAME)
            );
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getContainerInfo()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerInfo()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerInfo() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get statistics of a single container. This includes information about the number of collects and washes.
     * @param containerId identifier of the container to search
     * @return an instance of ContainerStatistics
     * @throws SQLException
     */
    public ContainerStatistics getContainerStatistics(int containerId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_STATISTICS)) {
            st.setInt(1, containerId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent container statistics");
                return null;
            }
            return new ContainerStatistics(rs.getInt(CONTAINER_ID_COLUMN_NAME),
                    rs.getInt(NUMBER_OF_WASHES_COLUMN_NAME), rs.getInt(NUMBER_OF_COLLECTS_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getContainerStatistics()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerStatistics()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerStatistics() " +
                        "because it was null");
            }
        }
    }

    /**
     * Returns all the data available for a specific container identified by an iot id
     * @param iotId identifier of the container
     * @return an instance of GetContainerInfo
     * @throws SQLException
     */
    public Container getContainerByIotId(String iotId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_BY_IOT_ID)) {
            st.setString(1, iotId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent container by its iot id");
                return null;
            }
            Timestamp lastReadDate = rs.getTimestamp(LAST_READ_DATE_COLUMN_NAME);
            return new Container(rs.getInt(CONTAINER_ID_COLUMN_NAME), rs.getString(IOT_ID_COLUMN_NAME),
                    rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                    rs.getShort(HEIGHT_COLUMN_NAME), rs.getString(CONTAINER_TYPE_COLUMN_NAME), lastReadDate == null ? null : lastReadDate.toLocalDateTime(),
                    rs.getShort(OCCUPATION_COLUMN_NAME), rs.getShort(TEMPERATURE_COLUMN_NAME), rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME),
                    rs.getInt(CONFIGURATION_ID_COLUMN_NAME), rs.getShort(BATTERY_COLUMN_NAME)
            );
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getContainerByIotId()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerByIotId()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @ContainerMapper.getContainerByIotId() " +
                        "because it was null");
            }
        }
    }

    /**
     * Returns the percentage of the containers in the whole system which occupation value is on the
     * desired range
     * @param min minimum value of the range, inclusive
     * @param max maximum value of the range, inclusive
     * @return an int between 0 and 100 that represent the percentage of containers
     * which the occupation is on the given range
     */
    public float getContainersWithOccupationBetweenRange(int min, int max) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(GET_CONTAINERS_WITH_OCCUPATION_BETWEEN_RANGE)) {
            st.registerOutParameter(1, Types.DECIMAL);
            st.setInt(2, min);
            st.setInt(3, max);
            st.execute();

            return st.getFloat(1);
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getContainersWithOccupationBetweenRange()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns the percentage of the containers in one route which occupation value is on the
     * desired range
     * @param min minimum value of the range, inclusive
     * @param max maximum value of the range, inclusive
     * @return an int between 0 and 100 that represent the percentage of containers
     * which the occupation is on the given range
     */
    public float getContainersOfARouteWithOccupationBetweenRange(int routeId, int min, int max) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(GET_CONTAINERS_OF_ROUTE_WITH_OCCUPATION_BETWEEN_RANGE)) {
            st.registerOutParameter(1, Types.FLOAT);
            st.setInt(2, routeId);
            st.setInt(3, min);
            st.setInt(4, max);
            st.execute();

            return st.getFloat(1);
        } catch (SQLException e) {
            log.warn("Error on @ContainerMapper.getContainersOfARouteWithOccupationBetweenRange()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }
}
