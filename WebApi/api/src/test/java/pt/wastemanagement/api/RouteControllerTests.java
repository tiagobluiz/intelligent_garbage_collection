package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.RouteCollectionController;
import pt.wastemanagement.api.controllers.RouteController;
import pt.wastemanagement.api.controllers.StationController;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.mappers.ContainerMapper;
import pt.wastemanagement.api.requester_implementations.RouteRequesterImplementation;
import pt.wastemanagement.api.views.input.RouteInput;
import pt.wastemanagement.api.views.output.GetRoute;
import pt.wastemanagement.api.views.output.collection_json.CollectionJson;
import pt.wastemanagement.api.views.output.collection_json.Property;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.TestCase.*;
import static pt.wastemanagement.api.requester_implementations.RouteRequesterImplementation.*;
import static pt.wastemanagement.api.controllers.Controller.PAGE_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.ROWS_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.SHOW_INACTIVE_QUERY_PARAM;

public class RouteControllerTests {

    @Test
    public void createRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int stationId = 1;
        RouteInput routeInput = new RouteInput();
        routeInput.startPoint = routeInput.finishPoint = stationId;

        ResponseEntity res = null;
        try {
            res = routeController.createRoute(routeInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());

        assertEquals(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + ROUTE_ID),
                res.getHeaders().get(HttpHeaders.LOCATION).get(0));
    }

    @Test
    public void createRouteWithNonExistentStationTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(WRONG_PARAMETERS_STATE));
        int stationId = 2;
        RouteInput routeInput = new RouteInput();
        routeInput.startPoint = routeInput.finishPoint = stationId;

        try {
            routeController.createRoute(routeInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int routeId = 1, startStationId = 1, finishStationId = 3;
        RouteInput routeInput = new RouteInput();
        routeInput.startPoint = startStationId;
        routeInput.finishPoint = finishStationId;

        ResponseEntity res = null;
        try {
            res = routeController.updateRoute(routeId, routeInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }


    @Test
    public void updateRouteWithNonExistentStationTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(WRONG_PARAMETERS_STATE));
        int routeId = 1, startStationId = 2, finishStationId = 3;
        RouteInput routeInput = new RouteInput();
        routeInput.startPoint = startStationId;
        routeInput.finishPoint = finishStationId;

        try {
            routeController.updateRoute(routeId, routeInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }


    @Test
    public void updateNonExistentRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(BAD_REQUEST_STATE));
        int routeId = 2, startStationId = 1, finishStationId = 3;
        RouteInput routeInput = new RouteInput();
        routeInput.startPoint = startStationId;
        routeInput.finishPoint = finishStationId;

        try {
            routeController.updateRoute(routeId, routeInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void activateRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int routeId = 1;

        ResponseEntity res = null;
        try {
            res = routeController.activateRoute(routeId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void activatNonExistentRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(BAD_REQUEST_STATE));
        int routeId = 2;

        try {
            routeController.activateRoute(routeId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }

    }

    @Test
    public void deactivateRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int routeId = 1;

        ResponseEntity res = null;
        try {
            res = routeController.deactivateRoute(routeId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void deactivateNonExistentRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(BAD_REQUEST_STATE));
        int routeId = 2;

        try {
            routeController.deactivateRoute(routeId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }

    }

    @Test
    public void getAllRoutesTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int page = 1, rows = 10;
        boolean showInactive = false;

        CollectionJson collection = null;
        try {
            collection = routeController.getAllRoutes(page, rows, showInactive).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = RouteController.GET_ALL_ROUTES_PATH + "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" +
                rows + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(RouteController.START_POINT_FIELD_NAME, empty(),
                of(RouteController.START_POINT_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteController.FINISH_POINT_FIELD_NAME, empty(),
                of(RouteController.FINISH_POINT_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(RouteController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID),
                of(RouteController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(RouteController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.START_POINT_STATION_NAME_FIELD_NAME, of(STATION_NAME),
                of(RouteController.START_POINT_STATION_NAME_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.START_POINT_LATITUDE_FIELD_NAME, of("" + LATITUDE),
                of(RouteController.START_POINT_LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.START_POINT_LONGITUDE_FIELD_NAME, of("" + LONGITUDE),
                of(RouteController.START_POINT_LONGITUDE_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.FINISH_POINT_STATION_NAME_FIELD_NAME, of(STATION_NAME),
                of(RouteController.FINISH_POINT_STATION_NAME_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.FINISH_POINT_LATITUDE_FIELD_NAME, of("" + LATITUDE),
                of(RouteController.FINISH_POINT_LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.FINISH_POINT_LONGITUDE_FIELD_NAME, of("" + LONGITUDE),
                of(RouteController.FINISH_POINT_LONGITUDE_TITLE), empty()));


        assertEquals(TOTAL_ROUTES, collection.items.size());

        int[] propertiesChecked = new int[]{0};
        collection.items.stream()
                .flatMap(item -> item.data.stream())
                .forEach(property ->{
                    assertTrue(expectedProperties.stream().anyMatch(prop ->
                            prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                                    Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
                    ));
                    propertiesChecked[0]++;
                });

        assertEquals(expectedProperties.size(), propertiesChecked[0]);
    }

    @Test
    public void getCollectableRoutes(){
        ApiApplication.initApplication();
        RouteCollectionController routeController = new RouteCollectionController(null, new RouteRequesterImplementation(NORMAL_STATE),
                null);
        int page = 1, rows = 10;
        String type = "paper";

        CollectionJson collection = null;
        try {
            collection = routeController.getCollectableRoutes(page, rows, type).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = RouteCollectionController.GET_COLLECTABLE_ROUTES_PATH + "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" +
                rows + "&" + RouteCollectionController.CONTAINER_TYPE_QUERY_PARAM + "=" + type;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(RouteCollectionController.LATITUDE_FIELD_NAME, empty(),
                of(RouteCollectionController.LATITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.LONGITUDE_FIELD_NAME, empty(),
                of(RouteCollectionController.LONGITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.START_DATE_FIELD_NAME, empty(),
                of(RouteCollectionController.START_DATE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, empty(),
                of(RouteCollectionController.TRUCK_PLATE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.CONTAINER_TYPE_FIELD_NAME, empty(),
                of(RouteCollectionController.CONTAINER_TYPE_TITLE), of(ContainerMapper.CONTAINER_TYPES)));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(RouteController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID),
                of(RouteController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(RouteController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.START_POINT_FIELD_NAME, of("" + STATION_ID),
                of(RouteController.START_POINT_TITLE), empty()));
        expectedProperties.add(new Property(RouteController.FINISH_POINT_FIELD_NAME, of("" + STATION_ID),
                of(RouteController.FINISH_POINT_TITLE), empty()));;


        assertEquals(TOTAL_ROUTES, collection.items.size());

        int[] propertiesChecked = new int[]{0};
        collection.items.stream()
                .flatMap(item -> item.data.stream())
                .forEach(property ->{
                    assertTrue(expectedProperties.stream().anyMatch(prop ->
                            prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                                    Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
                    ));
                    propertiesChecked[0]++;
                });

        assertEquals(expectedProperties.size(), propertiesChecked[0]);
    }

    @Test
    public void getRouteTest(){
        ApiApplication.initApplication();
        RouteController routeController = new RouteController(new RouteRequesterImplementation(NORMAL_STATE));
        int routeId = 1;

        ResponseEntity res = null;
        try {
            res = routeController.getRoute(routeId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.ROUTE_CLASS, siren._class[0]);

        //Properties
        GetRoute route = (GetRoute) siren.properties;

        assertEquals(routeId, route.routeId);
        assertEquals(ACTIVE.equalsIgnoreCase("T") ? Boolean.toString(true ): Boolean.toString(false), route.active);
        assertEquals(STATION_NAME, route.startPointStationName);
        assertEquals(LATITUDE, route.startPointLatitude);
        assertEquals(LONGITUDE, route.startPointLongitude);
        assertEquals(STATION_NAME, route.finishPointStationName);
        assertEquals(LATITUDE, route.finishPointLatitude);
        assertEquals(LONGITUDE, route.finishPointLongitude);
        assertEquals(NUM_COLLECT_ZONES, route.numCollectZones);
        assertEquals(NUM_COLLECTS, route.numCollects);
        assertEquals(NUM_CONTAINERS, route.numContainers);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(RouteController.UPDATE_ROUTE_ACTION_NAME);
        expectedActionsNames.add(RouteController.CREATE_ROUTE_COLLECT_ACTION_NAME);
        expectedActionsNames.add(RouteController.ACTIVATE_ROUTE_ACTION_NAME);
        expectedActionsNames.add(RouteController.DEACTIVATE_ROUTE_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_CONTAINERS_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_COLLECT_ZONES_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_COLLECTIONS_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_COLLECTION_PLAN_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_DROP_ZONE_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId));
        expectedLinks.add(RouteController.GET_ALL_ROUTES_PATH);
        expectedLinks.add(StationController.GET_ALL_STATIONS_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}