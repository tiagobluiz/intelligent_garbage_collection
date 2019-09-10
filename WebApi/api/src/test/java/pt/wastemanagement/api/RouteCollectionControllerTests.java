package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.RouteCollectionController;
import pt.wastemanagement.api.controllers.RouteController;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLPermissionDeniedException;
import pt.wastemanagement.api.model.RouteCollection;
import pt.wastemanagement.api.requester_implementations.RouteCollectionRequesterImplementation;
import pt.wastemanagement.api.views.input.CollectRouteInput;
import pt.wastemanagement.api.views.input.RouteCollectionInput;
import pt.wastemanagement.api.views.output.collection_json.CollectionJson;
import pt.wastemanagement.api.views.output.collection_json.Property;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static pt.wastemanagement.api.controllers.Controller.PAGE_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.ROWS_QUERY_PARAM;
import static pt.wastemanagement.api.requester_implementations.RouteCollectionRequesterImplementation.*;

/**
 * Methods that are form routeRequester and collectZoneRequester are tested on the same class where they were declared.
 * routeRequester > RouteControllerTests
 * collectZoneRequester > CollectZoneControllerTests
 */
public class RouteCollectionControllerTests {

    @Test
    public void createRouteCollectionTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(NORMAL_STATE), null, null);

        int routeId = 1;
        LocalDateTime startDate = LocalDateTime.now();
        String truckPlate = "FE-FE-FE";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.startDate = startDate;

        ResponseEntity res = null;
        try {
            res = routeCollectionController.createRouteCollection(routeId, routeCollectionInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void createRouteCollectionWithNonExistentRouteTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(WRONG_PARAMETERS_STATE), null, null);

        int routeId = 2;
        LocalDateTime startDate = LocalDateTime.now();
        String truckPlate = "FE-FE-FE";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.startDate = startDate;

        try {
            routeCollectionController.createRouteCollection(routeId, routeCollectionInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }

    }

    @Test
    public void createRouteCollectionWithRouteAlreadyBeingCollectedTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);

        int routeId = 3;
        LocalDateTime startDate = LocalDateTime.now();
        String truckPlate = "FE-FE-FE";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.startDate = startDate;

        try {
            routeCollectionController.createRouteCollection(routeId, routeCollectionInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLPermissionDeniedException);
        }

    }

    @Test
    public void collectRouteTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(NORMAL_STATE), null, null);

        float latitude = 0, longitude = 0;
        LocalDateTime startDate = LocalDateTime.now();
        String truckPlate = "FE-FE-FE", type = "general";
        CollectRouteInput routeCollectionInput = new CollectRouteInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.startDate = startDate;
        routeCollectionInput.latitude = latitude;
        routeCollectionInput.longitude = longitude;
        routeCollectionInput.containerType = type;

        ResponseEntity res = null;
        try {
            res = routeCollectionController.collectRoute(routeCollectionInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());

        assertEquals(RouteCollectionController.GET_ROUTE_COLLECTION_PATH.replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + ROUTE_ID)
                .replace(RouteCollectionController.START_DATE_PATH_VAR, startDate.toString()),
                res.getHeaders().get(HttpHeaders.LOCATION).get(0));
    }

    @Test
    public void collectRouteWithNonExistentTruck(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);

        float latitude = 0, longitude = 0;
        LocalDateTime startDate = LocalDateTime.now();
        String truckPlate = "FE-FE-FE", type = "general";
        CollectRouteInput routeCollectionInput = new CollectRouteInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.startDate = startDate;
        routeCollectionInput.latitude = latitude;
        routeCollectionInput.longitude = longitude;
        routeCollectionInput.containerType = type;

        try {
            routeCollectionController.collectRoute(routeCollectionInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateRouteCollectionTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(NORMAL_STATE), null, null);

        int routeId = 1;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime finishDate = startDate;
        String truckPlate = "FE-FE-FE";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.finishDate = finishDate;

        ResponseEntity res = null;
        try {
            res = routeCollectionController.updateRouteCollection(routeId, startDate, routeCollectionInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateRouteCollectionWithNonExistentTruckTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(WRONG_PARAMETERS_STATE), null, null);

        int routeId = 1;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime finishDate = startDate;
        String truckPlate = "NO-TF-OU";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.finishDate = finishDate;

        try {
            routeCollectionController.updateRouteCollection(routeId, startDate, routeCollectionInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateNonExistentRouteCollectionTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);

        int routeId = 2;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime finishDate = startDate;
        String truckPlate = "FE-FE-FE";
        RouteCollectionInput routeCollectionInput = new RouteCollectionInput();
        routeCollectionInput.truckPlate = truckPlate;
        routeCollectionInput.finishDate = finishDate;

        try {
            routeCollectionController.updateRouteCollection(routeId, startDate, routeCollectionInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getRouteCollectionsTest() throws Exception {
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);

        int routeId = 1, page = 1, rows = 10;

        CollectionJson collection = routeCollectionController.getRouteCollections(page, rows, routeId).collection;

        String expectedURI =  RouteCollectionController.GET_ROUTE_COLLECTIONS_LIST_PATH.replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows;

        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(RouteCollectionController.START_DATE_FIELD_NAME, empty(), of(RouteCollectionController.START_DATE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, empty(), of(RouteCollectionController.TRUCK_PLATE_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(RouteCollectionController.ROUTE_ID_FIELD_NAME, of("" + routeId), of(RouteCollectionController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.START_DATE_FIELD_NAME, of(DATE), of(RouteCollectionController.START_DATE_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.FINISH_DATE_FIELD_NAME, empty(), of(RouteCollectionController.FINISH_DATE_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, of(TRUCK_PLATE), of(RouteCollectionController.TRUCK_PLATE_TITLE), empty()));

        assertEquals(TOTAL_COLLECTIONS, collection.items.size());

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
    public void getTruckCollectionsTest() throws Exception {
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);

        int page = 1, rows = 10;
        String truckPlate = "AB-CD-EF";

        CollectionJson collection = routeCollectionController.getTruckCollections(page, rows, truckPlate).collection;

        String expectedURI =  RouteCollectionController.GET_TRUCK_COLLECTIONS_PATH.replace(RouteCollectionController.TRUCK_PLATE_PATH_VAR, truckPlate) +
                "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows;

        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(RouteCollectionController.START_DATE_FIELD_NAME, empty(), of(RouteCollectionController.START_DATE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, of(TRUCK_PLATE), of(RouteCollectionController.TRUCK_PLATE_TITLE),
                empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(RouteCollectionController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID), of(RouteCollectionController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.START_DATE_FIELD_NAME, of(DATE), of(RouteCollectionController.START_DATE_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.FINISH_DATE_FIELD_NAME, empty(), of(RouteCollectionController.FINISH_DATE_TITLE), empty()));
        expectedProperties.add(new Property(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, of(truckPlate), of(RouteCollectionController.TRUCK_PLATE_TITLE), empty()));

        assertEquals(TOTAL_COLLECTIONS, collection.items.size());

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
    public void getRouteCollectionTest(){
        ApiApplication.initApplication();
        RouteCollectionController routeCollectionController =
                new RouteCollectionController(new RouteCollectionRequesterImplementation(BAD_REQUEST_STATE), null, null);
        int routeId = 1;
        LocalDateTime startDate = LocalDateTime.now();

        ResponseEntity res = null;
        try {
            res = routeCollectionController.getRouteCollection(routeId, startDate);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.ROUTE_COLLECTIONS_CLASS, siren._class[0]);

        //Properties
        RouteCollection routeCollection = (RouteCollection) siren.properties;

        assertEquals(routeId, routeCollection.routeId);
        assertEquals(startDate, routeCollection.startDate);
        assertEquals(null, routeCollection.finishDate);
        assertEquals(TRUCK_PLATE, routeCollection.truckPlate);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(RouteCollectionController.UPDATE_ROUTE_COLLECTION_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.ROUTE_COLLECTION_PLAN_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(RouteCollectionController.GET_ROUTE_COLLECTION_PATH
                .replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId)
                .replace(RouteCollectionController.START_DATE_PATH_VAR, startDate.toString()));
        expectedLinks.add(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}