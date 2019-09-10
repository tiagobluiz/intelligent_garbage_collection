package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLPermissionDeniedException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.RouteCollection;
import pt.wastemanagement.api.requesters.RouteCollectionRequester;

import javax.inject.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RouteCollectionMapper implements RouteCollectionRequester {
    public static final String
    //Route Collection Table
            ROUTE_ID_COLUMN_NAME = "route_id",
            START_DATE_COLUMN_NAME = "start_date",
            FINISH_DATE_COLUMN_NAME = "finish_date",
            TRUCK_PLATE_COLUMN_NAME = "truck_plate",
    //Generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_ROUTE_COLLECTION = "{call dbo.CreateRouteCollection(?,?,?)}", //route_id, truck_plate, start_date
            COLLECT_ROUTE = "{call dbo.CollectRoute(?,?,?,?,?,?)}", //latitude, longitude, truck_plate, start_date, container_type, route_id OUT
            UPDATE_ROUTE_COLLECTION = "{call dbo.UpdateRouteCollection(?,?,?,?)}", //route_id, start_date, finish_date, truck_plate
            GET_ROUTE_COLLECTIONS = "SELECT * FROM dbo.GetRouteCollections(?,?,?)", //page, rows, route_id
            GET_ROUTE_COLLECTION = "SELECT * FROM dbo.GetRouteCollection(?,?)", //route_id, start_date
            GET_TRUCK_COLLECTS = "SELECT * FROM dbo.GetTruckCollections(?,?,?)"; //page, rows, truck_plate

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(RouteCollectionMapper.class);

    public RouteCollectionMapper (Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new route collection
     * @param routeId identifier of the route to be collected
     * @param startDate time instant (date & hour) when the collect begun
     * @param truckPlate truck that will do this collect
     * @throws SQLInvalidDependencyException if the route or truck doesn't exists or one or both
     *                                      are marked as inactive
     * @throws SQLPermissionDeniedException if the route is already being collected, this is
     *                                     recognizable by observing the most recent entry for
     *                                     the target route, seeing if the finish date is still null
     * @throws SQLException
     */
    public void createRouteCollection (int routeId, LocalDateTime startDate, String truckPlate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_ROUTE_COLLECTION)){
            st.setInt(1, routeId);
            st.setString(2, truckPlate);
            st.setTimestamp(3, Timestamp.valueOf(startDate));
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.createRouteCollection()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the route or the truck is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Creates a new route collection, where the route to be collected is chosen by the system
     * @param latitude latitude coordinates of the current user localization
     * @param longitude longitude coordinates of the current user localization
     * @param startDate time instant (date & hour) when the collect begun
     * @param truckPlate truck that will do this collect
     * @returns an integer with the route selected by the system
     * @throws SQLInvalidDependencyException if the route or truck doesn't exists or one or both
     *                                      are marked as inactive
     * @throws SQLPermissionDeniedException if the route is already being collected, this is
     *                                     recognizable by observing the most recent entry for
     *                                     the target route, seeing if the finish date is still null
     * @throws SQLException
     */
    public int collectRoute (float latitude, float longitude, LocalDateTime startDate, String truckPlate, String containerType)
            throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(COLLECT_ROUTE)){
            st.setFloat(1, latitude);
            st.setFloat(2, longitude);
            st.setString(3, truckPlate);
            st.setTimestamp(4, Timestamp.valueOf(startDate));
            st.setString(5, containerType);
            st.registerOutParameter(6, Types.INTEGER);
            st.execute();
            return st.getInt(6);
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.collectRoute()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("There is no routes available to be collected or " +
                        "the truck is already collecting another route, or this route is already being collected");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates a route collection. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param routeId route identifier
     * @param startDate start date identifier
     * @param finishDate new finish date
     * @param truckPlate new registration plate of the truck that make the collect
     * @throws SQLInvalidDependencyException if the truck doesn't exists or is currently inactive
     * @throws SQLNonExistentEntryException if the entry with the identified by the pair
     *                                     (@routeId, @startDate) doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateRouteCollectionTruck (int routeId, LocalDateTime startDate, LocalDateTime finishDate,
                                 String truckPlate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_ROUTE_COLLECTION)){
            st.setInt(1, routeId);
            st.setTimestamp(2, Timestamp.valueOf(startDate));
            st.setTimestamp(3, Timestamp.valueOf(finishDate));
            st.setString(4, truckPlate);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.updateRouteCollectionTruck()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The truck doesn't exists or is inactive");
            else if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Try to update a non existent entry on RouteCollection table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * All the collects of a given route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @return a list with a maximum of @rowsPerPage elements representing all collects of a route
     * @throws SQLException
     */
    public PaginatedList<RouteCollection> getRouteCollections (int pageNumber, int rowsPerPage, int routeId) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<RouteCollection> routeCollections = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_COLLECTIONS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, routeId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routeCollections);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                Timestamp finishDate =  rs.getTimestamp(FINISH_DATE_COLUMN_NAME);
                routeCollections.add(new RouteCollection(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getTimestamp(START_DATE_COLUMN_NAME).toLocalDateTime(),
                        finishDate == null? null : finishDate.toLocalDateTime(),rs.getString(TRUCK_PLATE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, routeCollections);
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.getRouteCollections()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteCollectionMapper.getRouteCollections()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @RouteCollectionMapper.getRouteCollections() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get statistics about a route, like number of containers, collect zones and collects
     * @param routeId identifier of the route to search
     * @param startDate date & time where the collection started
     * @return an instance of the object RouteStatistics
     * @throws SQLException
     */
    public RouteCollection getRouteCollection (int routeId, LocalDateTime startDate) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_COLLECTION)) {
            st.setInt(1,routeId);
            st.setTimestamp(2, Timestamp.valueOf(startDate));
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent route collection");
                return null;
            }
            Timestamp finishDate =  rs.getTimestamp(FINISH_DATE_COLUMN_NAME);
            return new RouteCollection(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getTimestamp(START_DATE_COLUMN_NAME).toLocalDateTime(),
                    finishDate == null ? null : finishDate.toLocalDateTime(), rs.getString(TRUCK_PLATE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.getRouteCollection()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on  @RouteCollectionMapper.getRouteCollection()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on  @RouteCollectionMapper.getRouteCollection()" +
                        "because it was null");
            }
        }
    }

    /**
     * All the collects performed by a truck
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param truckPlate identifier of the truck to search
     * @return a list with a maximum of @rowsPerPage elements representing al the collects made by a
     *        specific truck
     * @throws SQLException
     */
    public PaginatedList<RouteCollection> getTruckCollections (int pageNumber, int rowsPerPage, String truckPlate)
            throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<RouteCollection> truckCollections = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_TRUCK_COLLECTS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setString(3, truckPlate);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, truckCollections);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do {
                Timestamp finishDate =  rs.getTimestamp(FINISH_DATE_COLUMN_NAME);
                truckCollections.add(new RouteCollection(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getTimestamp(START_DATE_COLUMN_NAME).toLocalDateTime(),
                        finishDate == null ? null : finishDate.toLocalDateTime(), rs.getString(TRUCK_PLATE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, truckCollections);
        } catch (SQLException e) {
            log.warn("Error on @RouteCollectionMapper.getTruckCollections()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteCollectionMapper.getTruckCollections()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @RouteCollectionMapper.getTruckCollections() " +
                        "because it was null");
            }
        }
    }
}
