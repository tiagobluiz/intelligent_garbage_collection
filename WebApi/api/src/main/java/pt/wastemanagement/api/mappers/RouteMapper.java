package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLPermissionDeniedException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.Route;
import pt.wastemanagement.api.model.functions.*;
import pt.wastemanagement.api.requesters.RouteRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class RouteMapper implements RouteRequester {
    /*Database Definition*/
    //From table Route
    public static final String
    //Route Table
            ROUTE_ID_COLUMN_NAME = "route_id",
            START_POINT_COLUMN_NAME = "start_point",
            FINISH_POINT_COLUMN_NAME = "finish_point",
            ACTIVE_COLUMN_NAME = "active",
    //Table Functions Generic Fields
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries",
    //Get All Routes & Active Routes
            START_POINT_STATION_NAME_COLUMN_NAME = "start_point_station_name",
            START_POINT_LATITUDE_COLUMN_NAME = "start_point_latitude",
            START_POINT_LONGITUDE_COLUMN_NAME = "start_point_longitude",
            FINISH_POINT_STATION_NAME_COLUMN_NAME = "finish_point_station_name",
            FINISH_POINT_LATITUDE_COLUMN_NAME = "finish_point_latitude",
            FINISH_POINT_LONGITUDE_COLUMN_NAME = "finish_point_longitude",
    //Get Route Statistics
            NUM_COLLECT_ZONES_COLUMN_NAME = "num_collect_zones",
            NUM_CONTAINERS_COLUMN_NAME = "num_containers",
            NUM_COLLECTS_COLUMN_NAME = "num_collects";

    /*Procedures & Functions call definition*/
    private static final String
            CREATE_ROUTE = "{call dbo.CreateRoute(?,?,?)}", //start_point,finish_point,route_id OUT
            UPDATE_ROUTE = "{call dbo.UpdateRoute(?,?,?)}", //route_id,start_point,finish_point
            ACTIVATE_ROUTE = "{call dbo.ActivateRoute(?)}", //route_id
            DEACTIVATE_ROUTE = "{call dbo.DeactivateRoute(?)}", //route_id
            GET_ALL_ROUTES = "SELECT * FROM dbo.GetAllRoutes(?,?)", //pageNumber, rows
            GET_ALL_ACTIVE_ROUTES = "SELECT * FROM dbo.GetAllActiveRoutes(?,?)", //pageNumber, rows
            GET_ROUTE_INFO = "SELECT * FROM dbo.GetRouteInfo(?)", //route_id
            GET_ROUTE_STATISTICS = "SELECT * FROM dbo.GetRouteStatistics(?)", //route_id
            GET_COLLECTABLE_ROUTES = "SELECT * FROM dbo.GetCollectableRoutes(?,?,?)"; //page, rows, container_type

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(RouteMapper.class);

    public RouteMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new active route
     * @param startPoint identifier of the station where this route begins
     * @param finishPoint identifier of the station where this route ends
     * @return numeric identifier of the created route
     * @throws SQLInvalidDependencyException if the identifier of the start or finish station
     *                                      leads to a non existent one
     * @throws SQLException
     */
    public int createRoute(int startPoint, int finishPoint) throws SQLException {
        try(CallableStatement st = conn.get().prepareCall(CREATE_ROUTE)){
            st.setInt(1, startPoint);
            st.setInt(2, finishPoint);
            st.registerOutParameter(3, Types.INTEGER);
            st.execute();
            return  st.getInt(3);
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.createRoute()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of start or finish station" +
                        " is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates a route
     * @param routeId identifier of the route to update
     * @param startPoint identifier of the new start point station
     * @param finishPoint identifier of the new finish point station
     * @throws SQLInvalidDependencyException if the identifier of the start or finish station
     *                                      leads to a non existent one
     * @throws SQLNonExistentEntryException if the entry with the identified by (@routeId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateRoute (int routeId, int startPoint, int finishPoint) throws SQLException {
        try(CallableStatement st = conn.get().prepareCall(UPDATE_ROUTE)){
            st.setInt(1, routeId);
            st.setInt(2, startPoint);
            st.setInt(3, finishPoint);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.updateRoute()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Route table");
                throw new SQLNonExistentEntryException();
            }else if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of start or finish station" +
                        " is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Activate a route. If its already active, will not  be produced any side effects.
     * @param routeId identifier of the route
     * @throws SQLNonExistentEntryException if the entry with the identified by (@routeId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void activateRoute (int routeId) throws SQLException {
        try(CallableStatement st = conn.get().prepareCall(ACTIVATE_ROUTE)){
            st.setInt(1, routeId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.activateRoute()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to activate a non existent entry on Route table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deactivates a route. To be able to do this operation. If it's already inactive,
     * will not  be produced any side effects.
     * @param routeId identifier of the route
     * @throws SQLNonExistentEntryException if the entry with the identified by (@routeId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deactivateRoute (int routeId) throws SQLException {
        try(CallableStatement st = conn.get().prepareCall(DEACTIVATE_ROUTE)){
            st.setInt(1, routeId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.deactivateRoute()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE) {
                log.warn("Warning: Try to deactivate a non existent entry on Route table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Gets all routes on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the routes
     * @throws SQLException
     */
    public PaginatedList<RouteWithStationNameAndLocation> getAllRoutes(int pageNumber, int rowsPerPage, boolean showInactive) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<RouteWithStationNameAndLocation> routes = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try(PreparedStatement st = conn.get().prepareStatement(showInactive ? GET_ALL_ROUTES : GET_ALL_ACTIVE_ROUTES)){
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routes);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                routes.add(new RouteWithStationNameAndLocation(rs.getInt(ROUTE_ID_COLUMN_NAME),
                        rs.getString(ACTIVE_COLUMN_NAME), rs.getString(START_POINT_STATION_NAME_COLUMN_NAME),
                        rs.getFloat(START_POINT_LATITUDE_COLUMN_NAME), rs.getFloat(START_POINT_LONGITUDE_COLUMN_NAME),
                        rs.getString(FINISH_POINT_STATION_NAME_COLUMN_NAME), rs.getFloat(FINISH_POINT_LATITUDE_COLUMN_NAME),
                        rs.getFloat(FINISH_POINT_LONGITUDE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, routes);
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.getAllRoutes()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteMapper.getAllRoutes()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on @RouteMapper.getAllRoutes() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get information about a route, like start and finish point location
     * @param routeId identifier of the route to search
     * @return an instance of the object RouteWithStationNameAndLocation
     * @throws SQLException
     */
    public RouteWithStationNameAndLocation getRouteInfo (int routeId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_INFO)) {
            st.setInt(1,routeId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent route info");
                return null;
            }
            return new RouteWithStationNameAndLocation(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME),
                    rs.getString(START_POINT_STATION_NAME_COLUMN_NAME),rs.getFloat(START_POINT_LATITUDE_COLUMN_NAME),
                    rs.getFloat(START_POINT_LONGITUDE_COLUMN_NAME), rs.getString(FINISH_POINT_STATION_NAME_COLUMN_NAME),
                    rs.getFloat(FINISH_POINT_LATITUDE_COLUMN_NAME),rs.getFloat(FINISH_POINT_LONGITUDE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.getRouteInfo()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on  @RouteMapper.getRouteInfo()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on  @RouteMapper.getRouteInfo()" +
                        "because it was null");
            }
        }
    }

    /**
     * Get statistics about a route, like number of containers, collect zones and collects
     * @param routeId identifier of the route to search
     * @return an instance of the object RouteStatistics
     * @throws SQLException
     */
    public RouteStatistics getRouteStatistics (int routeId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_STATISTICS)) {
            st.setInt(1,routeId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent route statistics");
                return null;
            }
            return new RouteStatistics(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getInt(NUM_CONTAINERS_COLUMN_NAME),
                    rs.getInt(NUM_COLLECT_ZONES_COLUMN_NAME), rs.getInt(NUM_COLLECTS_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.getRouteStatistics()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on  @RouteMapper.getRouteStatistics()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on  @RouteMapper.getRouteStatistics()" +
                        "because it was null");
            }
        }
    }

    /**
     * Get all routes that can be collected
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerType type of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the routes
     * @throws SQLException
     */
    public PaginatedList<Route> getCollectableRoutes(int pageNumber, int rowsPerPage, String containerType) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Route> routes = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try(PreparedStatement st = conn.get().prepareStatement(GET_COLLECTABLE_ROUTES)){
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setString(3, containerType);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routes);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                routes.add(new Route(rs.getInt(ROUTE_ID_COLUMN_NAME), rs.getInt(START_POINT_COLUMN_NAME),
                        rs.getInt(FINISH_POINT_COLUMN_NAME), rs.getString(ACTIVE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, routes);
        } catch (SQLException e) {
            log.warn("Error on @RouteMapper.getCollectableRoutes()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteMapper.getCollectableRoutes()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on @RouteMapper.getCollectableRoutes() " +
                        "because it was null");
            }
        }
    }
}
