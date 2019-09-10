package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.functions.*;
import pt.wastemanagement.api.requesters.CollectZoneRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class CollectZoneMapper implements CollectZoneRequester {

    public static final String
    //Collect Zone Table
            COLLECT_ZONE_ID_COLUMN_NAME = "collect_zone_id",
            PICK_ORDER_COLUMN_NAME = "pick_order",
            ROUTE_ID_COLUMN_NAME = "route_id",
            LATITUDE_COLUMN_NAME = "latitude",
            LONGITUDE_COLUMN_NAME = "longitude",
            ACTIVE_COLUMN_NAME = "active",
    //Get Collect Zone Info
            GENERAL_OCCUPATION = "general_occupation",
            PAPER_OCCUPATION = "paper_occupation",
            PLASTIC_OCCUPATION = "plastic_occupation",
            GLASS_OCCUPATION = "glass_occupation",
    //Get collect zone statistics
            NUMBER_OF_CONTAINERS_COLUMN_NAME = "num_containers",
    //Generic Columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_COLLECT_ZONE = "{call dbo.CreateCollectZone(?,?)}", // route_id, collect_zone_id OUT
            UPDATE_COLLECT_ZONE = "{call dbo.UpdateCollectZone(?,?)}", // collect_zone_id, route_id
            DEACTIVATE_COLLECT_ZONE = "{call dbo.DeactivateCollectZone(?)}", // collect_zone_id
            ACTIVATE_COLLECT_ZONE = "{call dbo.ActivateCollectZone(?)}", //collect_zone_id
            GET_ROUTE_COLLECT_ZONES = "SELECT * FROM dbo.GetRouteCollectZones(?,?,?)", // pageNumber, rows, route_id
            GET_ROUTE_ACTIVE_COLLECT_ZONES = "SELECT * FROM dbo.GetRouteActiveCollectZones(?,?,?)", // pageNumber, rows, route_id
            GET_COLLECT_ZONE_INFO = "SELECT * FROM dbo.GetCollectZoneInfo(?)",  // collect_zone_id
            GET_COLLECT_ZONE_STATISTICS = "SELECT * FROM dbo.GetCollectZoneStatistics(?)", // collect_zone_id
            GET_ROUTE_COLLECTION_PLAN = "SELECT * FROM dbo.GetRouteCollectionPlan(?,?,?,?)", //page, rows, route_id, container_type
            GET_COLLECT_ZONES_IN_RANGE = "SELECT * FROM dbo.GetCollectZonesInRange(?,?,?)"; //latitude, longitude, range

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(CollectZoneMapper.class);

    public CollectZoneMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new collect zone
     * @param routeId route where this collect zone will, initially, belong
     * @return the identifier of the created collect zone
     * @throws SQLInvalidDependencyException if the route doesn't exists
     * @throws SQLException
     */
    public int createCollectZone(int routeId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_COLLECT_ZONE)) {
            st.setInt(1, routeId);
            st.registerOutParameter(2, Types.INTEGER);
            st.execute();
            return st.getInt(2);
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.createCollectZone()! S" +
                    "QL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the route is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates the collect zone route. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param collectZoneId identifier of the collect zone
     * @param routeId the identifier of the new route
     * @throws SQLInvalidDependencyException if the route doesn't exists
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateCollectZone (int collectZoneId, int routeId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_COLLECT_ZONE)){
            st.setInt(1, collectZoneId);
            st.setInt(2, routeId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.updateCollectZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Collect Zone table");
                throw new SQLNonExistentEntryException();
            } else if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the route is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deactivates a collect zone. If its already inactive, will not  be produced any side effects.
     * @param collectZoneId identifier of the collect zone
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deactivateCollectZone (int collectZoneId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DEACTIVATE_COLLECT_ZONE)){
            st.setInt(1, collectZoneId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.deactivateCollectZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to deactivate a non existent entry on Collect Zone table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Activate a collect zone. If its already active, will not  be produced any side effects.
     * @param collectZoneId identifier of the collect zone
     * @throws SQLNonExistentEntryException if the entry with the identified by (@containerId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void activateCollectZone (int collectZoneId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(ACTIVATE_COLLECT_ZONE)){
            st.setInt(1, collectZoneId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.activateCollectZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to activate a non existent entry on Collect Zone table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns the collect zones of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the collect zones of a given route
     * @throws SQLException
     */
    public PaginatedList<CollectZoneWithLocation> getRouteCollectZones (int pageNumber, int rowsPerPage, int routeId, boolean showInactive) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<CollectZoneWithLocation> routeCollectZones = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(showInactive? GET_ROUTE_COLLECT_ZONES : GET_ROUTE_ACTIVE_COLLECT_ZONES)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, routeId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routeCollectZones);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                routeCollectZones.add(new CollectZoneWithLocation(rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME), rs.getInt(ROUTE_ID_COLUMN_NAME),
                        rs.getInt(PICK_ORDER_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME),
                        rs.getFloat(LONGITUDE_COLUMN_NAME)));
            }while (rs.next());
            return new PaginatedList<>(totalEntries,routeCollectZones);
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.getRouteCollectZones()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getRouteCollectZones()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getRouteCollectZones() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get information about a single collect zone, like location, and value of the highest occupation
     * @param collectZoneId identifier of the collect zone
     * @return an instance of CollectZoneWithLocationAndOccupationInfo
     * @throws SQLException
     */
    public CollectZoneWithLocationAndOccupationInfo getCollectZoneInfo (int collectZoneId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_COLLECT_ZONE_INFO)){
            st.setInt(1, collectZoneId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: unsuccessful try to obtain a Collect Zone info");
                return null;
            }
            return new CollectZoneWithLocationAndOccupationInfo(rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME), rs.getInt(ROUTE_ID_COLUMN_NAME),
                    rs.getInt(PICK_ORDER_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME),
                    rs.getFloat(LATITUDE_COLUMN_NAME),rs.getFloat(LONGITUDE_COLUMN_NAME),
                    rs.getShort(GENERAL_OCCUPATION), rs.getShort(PAPER_OCCUPATION),rs.getShort(PLASTIC_OCCUPATION),
                    rs.getShort(GLASS_OCCUPATION));
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.getCollectZoneInfo()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZoneInfo()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZoneInfo() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get statistics of a single collect zone. This includes information about the number of containers.
     * @param collectZoneId identifier of the collect zone to search
     * @return an instance of CollectZoneStatistics
     * @throws SQLException
     */
    public CollectZoneStatistics getCollectZoneStatistics (int collectZoneId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_COLLECT_ZONE_STATISTICS)) {
            st.setInt(1, collectZoneId);
            rs = st.executeQuery();
            if (!rs.next()) {
                log.warn("Warning: unsuccessful try to obtain a Collect Zone statistics");
                return null;
            }
            return new CollectZoneStatistics(rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME),
                    rs.getInt(NUMBER_OF_CONTAINERS_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.getCollectZoneStatistics()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZoneStatistics()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZoneStatistics() " +
                        "because it was null");
            }
        }
    }

    /**
     * Returns a list that represents the route collection plan if the collect was made
     * on the instant where the query was performed for a certain container type
     * @param routeId identifier of the route to search
     * @param containerType the type of the container to be collected. This can vary between the values
     *                     general, paper, plastic or glass
     * @return a list ordered by pick order with the collect zone id, latitude and longitude of the collect zones
     * that have conditions to be collected when the query was performed
     * @throws SQLException
     */
    public PaginatedList<CollectZoneWithLocation> getRouteCollectionPlan(int pageNumber, int rowsPerPage,
                                                                         int routeId, String containerType) throws SQLException {
        List<CollectZoneWithLocation> plan = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_COLLECTION_PLAN)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3,routeId);
            st.setString(4,containerType);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, plan);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do {
                plan.add(new CollectZoneWithLocation(rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME), rs.getInt(ROUTE_ID_COLUMN_NAME),
                        rs.getInt(PICK_ORDER_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME),
                        rs.getFloat(LONGITUDE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, plan);
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.routeCollectionPlan()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on  @CollectZoneMapper.routeCollectionPlan()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on  @CollectZoneMapper.routeCollectionPlan()" +
                        "because it was null");
            }
        }
    }

    /**
     * Retrieves all the collect zone that are in a given range
     * @param latitude latitude coordinates of current user location
     * @param longitude longitude coordinates of current user location
     * @param range range to search
     * @return a list that represents the collect zones on a given range having the current user location
     * @throws SQLException
     */
    public List<CollectZoneWithLocation> getCollectZonesInRange (float latitude, float longitude, int range) throws SQLException {
        List<CollectZoneWithLocation> collectZones = new ArrayList<>();
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_COLLECT_ZONES_IN_RANGE)) {
            st.setFloat(1, latitude);
            st.setFloat(2, longitude);
            st.setInt(3, range);
            rs = st.executeQuery();
            while (rs.next())
                collectZones.add(new CollectZoneWithLocation(rs.getInt(COLLECT_ZONE_ID_COLUMN_NAME), rs.getInt(ROUTE_ID_COLUMN_NAME),
                        rs.getInt(PICK_ORDER_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME), rs.getFloat(LATITUDE_COLUMN_NAME),
                        rs.getFloat(LONGITUDE_COLUMN_NAME)));

            return collectZones;
        } catch (SQLException e) {
            log.warn("Error on @CollectZoneMapper.getCollectZonesInRange()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZonesInRange()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectZoneMapper.getCollectZonesInRange() " +
                        "because it was null");
            }
        }
    }
}
