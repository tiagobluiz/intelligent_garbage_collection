package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Station;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.StationRequester;

import java.util.ArrayList;
import java.util.List;

public class StationRequesterImplementation implements StationRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public StationRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    public static final int TOTAL_STATIONS = 1, STATION_ID = 1;
    public static final String STATION_NAME = "Test Station", STATION_TYPE = "base";
    public static final float LATITUDE = 0, LONGITUDE = 0;
    @Override
    public int createStation(String stationName, float latitude, float longitude, String stationType) throws Exception {
        return STATION_ID;
    }

    @Override
    public void updateStation(int stationId, String stationName, float latitude, float longitude, String stationType) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Station didn't exists
        }
        return;
    }

    @Override
    public void deleteStation(int stationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Station didn't exists
        }
        return;
    }

    @Override
    public PaginatedList<Station> getAllStations(int pageNumber, int rowsPerPage) throws Exception {
        List<Station> stations = new ArrayList<>();
        stations.add(new Station(STATION_ID, STATION_NAME, LATITUDE, LONGITUDE, STATION_TYPE));
        return new PaginatedList<>(TOTAL_STATIONS, stations);
    }

    @Override
    public Station getStationInfo(int stationId) throws Exception {
        return new Station(stationId, STATION_NAME, LATITUDE, LONGITUDE, STATION_TYPE);
    }
}