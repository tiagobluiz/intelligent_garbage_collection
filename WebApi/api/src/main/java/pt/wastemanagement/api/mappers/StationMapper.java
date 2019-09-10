package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLDependencyBreakException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.Station;
import pt.wastemanagement.api.requesters.StationRequester;
import pt.wastemanagement.api.views.output.Options;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class StationMapper implements StationRequester {
    public static final String STATION_TABLE_NAME = "Station",
    //Station Table
            STATION_NAME_COLUMN_NAME = "station_name",
            STATION_ID_COLUMN_NAME = "station_id",
            LATITUDE_COLUMN_NAME = "latitude",
            LONGITUDE_COLUMN_NAME = "longitude",
            STATION_TYPE_COLUMN_NAME = "station_type",
    //Drop Zone Table
            DROP_ZONE_ID_COLUMN_NAME = "drop_zone_id",
    //Generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    //Station type
    public static final Options
            BASE_STATION_TYPE = new Options("Base", "base"),
            DROP_ZONE_TYPE = new Options("Drop Zone", "drop_zone");

    public static final List<Options> STATION_TYPES =
            Arrays.asList(new Options[]{BASE_STATION_TYPE, DROP_ZONE_TYPE});

    private static final String
            CREATE_STATION = "{call dbo.CreateStation(?,?,?,?,?)}", //station_name, latitude, longitude, station_type, station_id OUT
            UPDATE_STATION = "{call dbo.UpdateStation(?,?,?,?,?)}", //station_id, station_name, latitude, longitude, station_type
            DELETE_STATION = "{call dbo.DeleteStation(?)}", //station_id
            GET_ALL_STATIONS = "SELECT * FROM dbo.GetAllStations(?,?)", //page, rows
            GET_STATION_INFO = "SELECT * FROM dbo.GetStationInfo(?)"; //station_id

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(StationMapper.class);

    public StationMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new station
     * @param stationName new station name
     * @param latitude new latitude coordinates of the station location
     * @param longitude new longitude coordinates of the station location
     * @return identifier of the new station
     * @throws SQLException
     */
    public int createStation(String stationName, float latitude, float longitude, String stationType) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_STATION)) {
            st.setString(1, stationName);
            st.setFloat(2, latitude);
            st.setFloat(3, longitude);
            st.setString(4, stationType);
            st.registerOutParameter(5, Types.INTEGER);
            st.execute();
            return st.getInt(5);
        } catch (SQLException e) {
            log.warn("Error on @StationMapper.createStation()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates a station. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param stationId identifier of the station to update
     * @param stationName new name of the station
     * @param latitude new latitude coordinates of the station location
     * @param longitude new longitude coordinates of the station location
     * @throws SQLNonExistentEntryException if the entry with the identified by (@stationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLDependencyBreakException if one or more routes still have this station as drop zones
     * @throws SQLException
     */
    public void updateStation (int stationId, String stationName, float latitude, float longitude, String stationType) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_STATION)) {
            st.setInt(1, stationId);
            st.setString(2, stationName);
            st.setFloat(3, latitude);
            st.setFloat(4, longitude);
            st.setString(5, stationType);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @StationMapper.updateStation()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Station table");
                throw new SQLNonExistentEntryException();
            }else if(e.getErrorCode() == SQLDependencyBreakException.ERROR_CODE)
                throw new SQLDependencyBreakException("One or more routes still use this station as drop zone");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deletes a station.
     * @param stationId identifier of the station
     * @throws SQLDependencyBreakException if there's still one or more routes that use this station as
     *                                    start or finish point, or if a route uses this station as one of
     *                                    his drop zones.
     * @throws SQLException
     */
    public void deleteStation (int stationId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DELETE_STATION)) {
            st.setInt(1, stationId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @StationMapper.deleteStation()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLDependencyBreakException.ERROR_CODE)
                throw new SQLDependencyBreakException("There's still one or more routes that use this station");
            else if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to delete a non existent entry on Station table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Gets all stations of the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents all the existing stations
     * @throws SQLException
     */
    public PaginatedList<Station> getAllStations(int pageNumber, int rowsPerPage) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Station> stations = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try(PreparedStatement st = conn.get().prepareStatement(GET_ALL_STATIONS)){
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if(!rs.next()) return  new PaginatedList<>(totalEntries, stations);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                stations.add(new Station(rs.getInt(STATION_ID_COLUMN_NAME), rs.getString(STATION_NAME_COLUMN_NAME),
                        rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                        rs.getString(STATION_TYPE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, stations);
        } catch (SQLException e) {
            log.warn("Error on @StationMapper.getAllStations()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @StationMapper.getAllStations()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on @StationMapper.getAllStations() " +
                        "because it was null");
            }
        }
    }

    /**
     * Gets information about a station, like her location.
     * @param stationId identifier of the station to search
     * @return an instance of GetStationInfo
     * @throws SQLException
     */
    public Station getStationInfo (int stationId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_STATION_INFO)) {
            st.setInt(1, stationId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent station info");
                return null;
            }
            return new Station(rs.getInt(STATION_ID_COLUMN_NAME), rs.getString(STATION_NAME_COLUMN_NAME),
                    rs.getFloat(LATITUDE_COLUMN_NAME), rs.getFloat(LONGITUDE_COLUMN_NAME),
                    rs.getString(STATION_TYPE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @StationMapper.getStationInfo()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @StationMapper.getStationInfo()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @StationMapper.getStationInfo() " +
                        "because it was null");
            }
        }
    }
}
