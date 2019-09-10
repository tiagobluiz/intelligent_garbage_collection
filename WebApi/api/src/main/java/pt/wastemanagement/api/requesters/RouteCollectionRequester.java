package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.RouteCollection;
import pt.wastemanagement.api.model.utils.PaginatedList;

import java.time.LocalDateTime;

public interface RouteCollectionRequester {

    /**
     * Creates a new route collection
     * @param routeId identifier of the route to be collected
     * @param startDate time instant (date & hour) when the collect begun
     * @param truckPlate truck that will do this collect
     */
    void createRouteCollection (int routeId, LocalDateTime startDate, String truckPlate) throws Exception;

    /**
     * Creates a new route collection, where the route to be collected is chosen by the system
     * @param latitude latitude coordinates of the current user localization
     * @param longitude longitude coordinates of the current user localization
     * @param startDate time instant (date & hour) when the collect begun
     * @param truckPlate truck that will do this collect
     * @param containerType type of the container to be collected
     * @returns an integer with the route selected by the system
     */
    int collectRoute (float latitude, float longitude, LocalDateTime startDate, String truckPlate, String containerType) throws Exception;

    /**
     * Updates a route collection.
     * even if they keep the same.
     * @param routeId route identifier
     * @param startDate start date identifier
     * @param finishDate new finish date
     * @param truckPlate new registration plate of the truck that make the collect
     */
    void updateRouteCollectionTruck (int routeId, LocalDateTime startDate, LocalDateTime finishDate,
                                 String truckPlate) throws Exception;

    /**
     * All the collects of a given route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @return a list with a maximum of @rowsPerPage elements representing all collects of a route
     */
    PaginatedList<RouteCollection> getRouteCollections (int pageNumber, int rowsPerPage, int routeId) throws Exception;

    /**
     * Get statistics about a route, like number of containers, collect zones and collects
     * @param routeId identifier of the route to search
     * @param startDate date & time where the collection started
     * @return an instance of the object RouteStatistics
     */
    RouteCollection getRouteCollection (int routeId, LocalDateTime startDate) throws Exception;

    /**
     * All the collects performed by a truck
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param truckPlate identifier of the truck to search
     * @return a list with a maximum of @rowsPerPage elements representing al the collects made by a
     *        specific truck
     */
    PaginatedList<RouteCollection> getTruckCollections (int pageNumber, int rowsPerPage, String truckPlate) throws Exception;
}
