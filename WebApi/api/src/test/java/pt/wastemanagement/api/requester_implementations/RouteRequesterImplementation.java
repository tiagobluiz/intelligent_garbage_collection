package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Route;
import pt.wastemanagement.api.model.functions.RouteStatistics;
import pt.wastemanagement.api.model.functions.RouteWithStationNameAndLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.RouteRequester;

import java.util.ArrayList;
import java.util.List;

public class RouteRequesterImplementation implements RouteRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public RouteRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    public static final int ROUTE_ID = 1;

    @Override
    public int createRoute(int startPoint, int finishPoint) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // One of the stations were invalid
        }
        return ROUTE_ID;
    }

    @Override
    public void updateRoute(int routeId, int startPoint, int finishPoint) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); //One of the stations were invalid
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); //Route didn't exists
        }
        return;
    }

    @Override
    public void activateRoute(int routeId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Route didn't exists
        }
        return;
    }

    @Override
    public void deactivateRoute(int routeId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Route didn't exists
        }
        return;
    }

    public static final String STATION_NAME = "ABC";
    public static final float LATITUDE = 1, LONGITUDE = 1;
    public static final String ACTIVE = "T";
    public static final int TOTAL_ROUTES = 1, NUM_CONTAINERS = 2, NUM_COLLECTS = 50, NUM_COLLECT_ZONES = 1, STATION_ID = 1;
    @Override
    public PaginatedList<RouteWithStationNameAndLocation> getAllRoutes(int pageNumber, int rowsPerPage, boolean showInactive) throws Exception {
        List<RouteWithStationNameAndLocation> routes = new ArrayList<>();
        routes.add(new RouteWithStationNameAndLocation(ROUTE_ID, ACTIVE, STATION_NAME, LATITUDE, LONGITUDE, STATION_NAME, LATITUDE, LONGITUDE));

        return new PaginatedList<>(TOTAL_ROUTES, routes);
    }

    @Override
    public RouteWithStationNameAndLocation getRouteInfo(int routeId) throws Exception {
        return new RouteWithStationNameAndLocation(ROUTE_ID, ACTIVE, STATION_NAME, LATITUDE, LONGITUDE, STATION_NAME, LATITUDE, LONGITUDE);
    }

    @Override
    public RouteStatistics getRouteStatistics(int routeId) throws Exception {
        return new RouteStatistics(routeId, NUM_CONTAINERS, NUM_COLLECT_ZONES, NUM_COLLECTS);
    }

    @Override
    public PaginatedList<Route> getCollectableRoutes(int pageNumber, int rowsPerPage, String type) throws Exception {
        List<Route> routes = new ArrayList<>();
        routes.add(new Route(ROUTE_ID, STATION_ID, STATION_ID, ACTIVE));

        return new PaginatedList<>(TOTAL_ROUTES, routes);
    }
}