package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLPermissionDeniedException;
import pt.wastemanagement.api.model.RouteCollection;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.RouteCollectionRequester;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RouteCollectionRequesterImplementation implements RouteCollectionRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public RouteCollectionRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    @Override
    public void createRouteCollection(int routeId, LocalDateTime startDate, String truckPlate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); // Invalid route and/or truck
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLPermissionDeniedException(); // Route already being collected
        }
        return;
    }

    public static final int ROUTE_ID = 1;
    @Override
    public int collectRoute(float latitude, float longitude, LocalDateTime startDate, String truckPlate, String type) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE)
            throw new SQLInvalidDependencyException(); // Invalid route and/or truck
        return ROUTE_ID;
    }

    @Override
    public void updateRouteCollectionTruck(int routeId, LocalDateTime startDate, LocalDateTime finishDate, String truckPlate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); // Invalid truck
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); // Route Collection didn't exists
        }
        return;
    }

    public static final String DATE = "2018-05-05T21:21:21", TRUCK_PLATE = "AB-CD-EF";
    public static final int TOTAL_COLLECTIONS = 1;
    @Override
    public PaginatedList<RouteCollection> getRouteCollections(int pageNumber, int rowsPerPage, int routeId) throws Exception {
        List<RouteCollection> routeCollections = new ArrayList<>();
        routeCollections.add(new RouteCollection(routeId,LocalDateTime.parse(DATE),null, TRUCK_PLATE));

        return new PaginatedList<>(TOTAL_COLLECTIONS, routeCollections);
    }

    @Override
    public RouteCollection getRouteCollection(int routeId, LocalDateTime startDate) throws Exception {
        return new RouteCollection(routeId,startDate,null, TRUCK_PLATE);
    }

    @Override
    public PaginatedList<RouteCollection> getTruckCollections(int pageNumber, int rowsPerPage, String truckPlate) throws Exception {
        List<RouteCollection> routeCollections = new ArrayList<>();
        routeCollections.add(new RouteCollection(ROUTE_ID,LocalDateTime.parse(DATE), null, TRUCK_PLATE));

        return new PaginatedList<>(TOTAL_COLLECTIONS, routeCollections);
    }
}