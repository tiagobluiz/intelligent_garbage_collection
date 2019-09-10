package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.functions.CollectZoneStatistics;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocation;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocationAndOccupationInfo;
import pt.wastemanagement.api.model.utils.PaginatedList;

import java.util.List;

public interface CollectZoneRequester {

    /**
     * Creates a new collect zone
     * @param routeId route where this collect zone will, initially, belong
     * @return the identifier of the created collect zone
     */
    int createCollectZone(int routeId) throws Exception;

    /**
     * Updates the collect zone route.
     * @param collectZoneId identifier of the collect zone
     * @param routeId the identifier of the new route
     */
    void updateCollectZone (int collectZoneId, int routeId) throws Exception;

    /**
     * Deactivates a collect zone.
     * @param collectZoneId identifier of the collect zone
     */
    void deactivateCollectZone (int collectZoneId) throws Exception;

    /**
     * Activate a collect zone.
     * @param collectZoneId identifier of the collect zone
     */
    void activateCollectZone (int collectZoneId) throws Exception;

    /**
     * Returns the collect zones of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the collect zones of a given route
     */
    PaginatedList<CollectZoneWithLocation> getRouteCollectZones (int pageNumber, int rowsPerPage, int routeId,
                                                                 boolean showInactive) throws Exception;
    /**
     * Get information about a single collect zone, like location, and value of the highest occupation
     * @param collectZoneId identifier of the collect zone
     * @return an instance of CollectZoneWithLocationAndOccupationInfo
     */
    CollectZoneWithLocationAndOccupationInfo getCollectZoneInfo (int collectZoneId) throws Exception;

    /**
     * Get statistics of a single collect zone. This includes information about the number of containers.
     * @param collectZoneId identifier of the collect zone to search
     * @return an instance of CollectZoneStatistics
     */
    CollectZoneStatistics getCollectZoneStatistics (int collectZoneId) throws Exception;

    /**
     * Returns a list that represents the route collection plan if the collect was made
     * on the instant where the query was performed for a certain container type
     * @param routeId identifier of the route to search
     * @param containerType the type of the container to be collected. This can vary between the values
     *                     general, paper, plastic or glass
     * @return a list ordered by pick order with the collect zone id, latitude and longitude of the collect zones
     * that have conditions to be collected when the query was performed
     */
    PaginatedList<CollectZoneWithLocation> getRouteCollectionPlan(int pageNumber, int rowsPerPage,
                                                                  int routeId, String containerType) throws Exception;
    /**
     * Retrieves all the collect zone that are in a given range
     * @param latitude latitude coordinates of current user location
     * @param longitude longitude coordinates of current user location
     * @param range range to search
     * @return a list that represents the collect zones on a given range having the current user location
     */
    List<CollectZoneWithLocation> getCollectZonesInRange (float latitude, float longitude, int range) throws Exception;
}
