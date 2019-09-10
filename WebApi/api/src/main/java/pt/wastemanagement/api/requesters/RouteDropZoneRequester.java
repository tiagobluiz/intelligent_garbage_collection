package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.RouteDropZone;
import pt.wastemanagement.api.model.functions.RouteDropZoneWithLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface RouteDropZoneRequester {

    /**
     * Create a new route drop zone
     * @param routeId identifier of the route to associate to a drop zone
     * @param dropZoneId identifier of the station to associate to a route
     */
    void createRouteDropZone (int routeId, int dropZoneId) throws Exception;

    /**
     * Deletes a route drop zone identified by the pair (@routeId, @dropZoneId)
     * @param routeId identifier of the route
     * @param dropZoneId identifier of the
     */
    void deleteRouteDropZone (int routeId, int dropZoneId) throws Exception;

    /**
     * Get all drop zones of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @return a list with a maximum of @rowsPerPage elements that represents all the drop zones of a route
     */
    PaginatedList<RouteDropZoneWithLocation> getRouteDropZonesList(int pageNumber, int rowsPerPage, int routeId) throws Exception;

    /**
     * Get route drop zone info
     * @param routeId identifier of the route
     * @param dropZoneId identifier of the drop zone station
     * @return an instance of RouteDropZone
     */
    RouteDropZoneWithLocation getRouteDropZone(int routeId, int dropZoneId) throws Exception;
}
