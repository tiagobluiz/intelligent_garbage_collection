package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.RouteDropZone;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.functions.RouteDropZoneWithLocation;
import pt.wastemanagement.api.requesters.RouteDropZoneRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class RouteDropZoneMapper implements RouteDropZoneRequester {
    public static final String
            ROUTE_ID_COLUMN_NAME = "route_id",
            DROP_ZONE_ID_COLUMN_NAME = "drop_zone_id",
            LATITUDE_COLUMN_NAME = StationMapper.LATITUDE_COLUMN_NAME,
            LONGITUDE_COLUMN_NAME = StationMapper.LONGITUDE_COLUMN_NAME,
    // Generic Columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_ROUTE_DROP_ZONE = "{call dbo.CreateRouteDropZone(?,?)}", //route_id, drop_zone_id
            DELETE_ROUTE_DROP_ZONE = "{call dbo.DeleteRouteDropZone(?,?)", //route_id, drop_zone_id
            GET_ROUTE_DROP_ZONES_LIST = "SELECT * FROM dbo.GetRouteDropZones(?,?,?)", //page, rows, route_id
            GET_ROUTE_DROP_ZONE = "SELECT * FROM dbo.GetRouteDropZone(?,?)"; //route_id, drop_zone_id

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(RouteDropZoneMapper.class);

    public RouteDropZoneMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Create a new route drop zone
     * @param routeId identifier of the route to associate to a drop zone
     * @param dropZoneId identifier of the station to associate to a route
     * @throws SQLInvalidDependencyException if the route doesn't exists or is currently inactive, or
     *                                      the drop zone doesn't exists
     * @throws SQLException
     */
    public void createRouteDropZone (int routeId, int dropZoneId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_ROUTE_DROP_ZONE)) {
            st.setInt(1, routeId);
            st.setInt(2, dropZoneId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteDropZoneMapper.createRouteDropZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the route or drop zone is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deletes a route drop zone identified by the pair (@routeId, @dropZoneId)
     * @param routeId identifier of the route
     * @param dropZoneId identifier of the
     * @throws SQLNonExistentEntryException if the entry with the identified by the pair
     *                                    (@routeId, dropZoneId) doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deleteRouteDropZone (int routeId, int dropZoneId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DELETE_ROUTE_DROP_ZONE)) {
            st.setInt(1, routeId);
            st.setInt(2, dropZoneId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @RouteDropZoneMapper.deleteRouteDropZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to delete a non existent entry on Route Drop Zone table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Get all drop zones of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @return a list with a maximum of @rowsPerPage elements that represents all the drop zones of a route
     * @throws SQLException
     */
    public PaginatedList<RouteDropZoneWithLocation> getRouteDropZonesList(int pageNumber, int rowsPerPage, int routeId)
            throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<RouteDropZoneWithLocation> routeDropZones = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_DROP_ZONES_LIST)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, routeId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routeDropZones);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                routeDropZones.add(new RouteDropZoneWithLocation(rs.getInt(ROUTE_ID_COLUMN_NAME),
                        rs.getInt(DROP_ZONE_ID_COLUMN_NAME), rs.getInt(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME)));
            }while (rs.next());
            return new PaginatedList<>(totalEntries, routeDropZones);
        } catch (SQLException e) {
            log.warn("Error on @RouteDropZoneMapper.getRouteDropZonesList()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteDropZoneMapper.getRouteDropZonesList()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @RouteDropZoneMapper.getRouteDropZonesList() " +
                        "because it was null");
            }
        }
    }


    /**
     * Get route drop zone info
     * @param routeId identifier of the route
     * @param dropZoneId identifier of the drop zone station
     * @return an instance of RouteDropZone
     * @throws SQLException
     */
    public RouteDropZoneWithLocation getRouteDropZone(int routeId, int dropZoneId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ROUTE_DROP_ZONE)) {
            st.setInt(1, routeId);
            st.setInt(2, dropZoneId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent Route Drop Zone");
                return null;
            }
            return new RouteDropZoneWithLocation(rs.getInt(ROUTE_ID_COLUMN_NAME),
                    rs.getInt(DROP_ZONE_ID_COLUMN_NAME), rs.getInt(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @RouteDropZoneMapper.getRouteDropZone()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @RouteDropZoneMapper.getRouteDropZone()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @RouteDropZoneMapper.getRouteDropZone() " +
                        "because it was null");
            }
        }
    }
}
