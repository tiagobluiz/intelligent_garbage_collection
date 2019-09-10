package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Route;
import pt.wastemanagement.api.model.functions.RouteStatistics;
import pt.wastemanagement.api.model.functions.RouteWithStationNameAndLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface RouteRequester {

    /**
     * Creates a new active route
     * @param startPoint identifier of the station where this route begins
     * @param finishPoint identifier of the station where this route ends
     * @return numeric identifier of the created route
     */
    int createRoute(int startPoint, int finishPoint) throws Exception;

    /**
     * Updates a route
     * @param routeId identifier of the route to update
     * @param startPoint identifier of the new start point station
     * @param finishPoint identifier of the new finish point station
     */
    void updateRoute (int routeId, int startPoint, int finishPoint) throws Exception;

    /**
     * Activate a route. If its already active, will not  be produced any side effects.
     * @param routeId identifier of the route
     */
    void activateRoute (int routeId) throws Exception;

    /**
     * Deactivates a route.
     * @param routeId identifier of the route
     */
    void deactivateRoute (int routeId) throws Exception;

    /**
     * Gets all routes on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the routes
     */
    PaginatedList<RouteWithStationNameAndLocation> getAllRoutes(int pageNumber, int rowsPerPage, boolean showInactive) throws Exception;

    /**
     * Get information about a route, like start and finish point location
     * @param routeId identifier of the route to search
     * @return an instance of the object RouteWithStationNameAndLocation
     */
    RouteWithStationNameAndLocation getRouteInfo (int routeId) throws Exception;

    /**
     * Get statistics about a route, like number of containers, collect zones and collects
     * @param routeId identifier of the route to search
     * @return an instance of the object RouteStatistics
     */
    RouteStatistics getRouteStatistics (int routeId) throws Exception;

    /**
     * Get all routes that can be collected
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerType type of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the routes
     */
    PaginatedList<Route> getCollectableRoutes(int pageNumber, int rowsPerPage, String containerType) throws Exception;
}
