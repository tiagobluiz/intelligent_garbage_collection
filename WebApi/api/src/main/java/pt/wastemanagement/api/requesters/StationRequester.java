package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Station;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface StationRequester {

    /**
     * Creates a new station
     * @param stationName new station name
     * @param latitude new latitude coordinates of the station location
     * @param longitude new longitude coordinates of the station location
     * @return identifier of the new station
     */
    int createStation(String stationName, float latitude, float longitude, String stationType) throws Exception;

    /**
     * Updates a station.
     * even if they keep the same.
     * @param stationId identifier of the station to update
     * @param stationName new name of the station
     * @param latitude new latitude coordinates of the station location
     * @param longitude new longitude coordinates of the station location
     */
    void updateStation (int stationId, String stationName, float latitude, float longitude, String stationType) throws Exception;

    /**
     * Deletes a station.
     * @param stationId identifier of the station
     */
    void deleteStation (int stationId) throws Exception;

    /**
     * Gets all stations of the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents all the existing stations
     */
    PaginatedList<Station> getAllStations(int pageNumber, int rowsPerPage) throws Exception;

    /**
     * Gets information about a station, like her location.
     * @param stationId identifier of the station to search
     * @return an instance of GetStationInfo
     */
    Station getStationInfo (int stationId) throws Exception;
}
