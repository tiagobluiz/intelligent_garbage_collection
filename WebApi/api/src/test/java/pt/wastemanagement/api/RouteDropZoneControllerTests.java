package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.RouteDropZoneController;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.RouteDropZone;
import pt.wastemanagement.api.model.functions.RouteDropZoneWithLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.RouteDropZoneRequester;
import pt.wastemanagement.api.views.input.RouteDropZoneInput;
import pt.wastemanagement.api.views.output.collection_json.CollectionJson;
import pt.wastemanagement.api.views.output.collection_json.Property;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static pt.wastemanagement.api.RouteDropZoneControllerTests.RouteDropZoneRequesterImplementation.*;
import static pt.wastemanagement.api.controllers.Controller.PAGE_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.ROWS_QUERY_PARAM;

public class RouteDropZoneControllerTests {

    @Test
    public void createRouteDropZoneTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(NORMAL_STATE));
        int routeId = 1, dropZoneId = 2;
        RouteDropZoneInput routeDropZoneInput = new RouteDropZoneInput();
        routeDropZoneInput.dropZoneId = dropZoneId;

        ResponseEntity res = null;
        try {
            res = routeDropZoneController.createRouteDropZone(routeId, routeDropZoneInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void createRouteWithNonExistentDropZoneTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int routeId = 1, dropZoneId = 3;
        RouteDropZoneInput routeDropZoneInput = new RouteDropZoneInput();
        routeDropZoneInput.dropZoneId = dropZoneId;

        try {
            routeDropZoneController.createRouteDropZone(routeId, routeDropZoneInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }

    }

    @Test
    public void deleteRouteDropZoneTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(NORMAL_STATE));
        int routeId = 1, dropZoneId = 2;

        ResponseEntity res = null;
        try {
            res = routeDropZoneController.deleteRouteDropZone(routeId, dropZoneId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void deleteNonExistentRouteDropZoneTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(BAD_REQUEST_STATE));
        int routeId = 2, dropZoneId = 2;

        try {
            routeDropZoneController.deleteRouteDropZone(routeId, dropZoneId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }

    }

    @Test
    public void getAllRouteDropZonesTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(NORMAL_STATE));
        int page = 1, rows = 10, routeId = 1;
        CollectionJson collection = null;

        try {
            collection = routeDropZoneController.getRouteDropZonesList(page, rows, routeId).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = RouteDropZoneController.GET_ROUTE_DROP_ZONES_LIST_PATH.replace(RouteDropZoneController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(RouteDropZoneController.DROP_ZONE_ID_FIELD_NAME, empty(),
                of(RouteDropZoneController.DROP_ZONE_ID_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(RouteDropZoneController.ROUTE_ID_FIELD_NAME, of("" + routeId),
                of(RouteDropZoneController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteDropZoneController.DROP_ZONE_ID_FIELD_NAME, of("" + DROP_ZONE_ID),
                of(RouteDropZoneController.DROP_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(RouteDropZoneController.LATITUDE_FIELD_NAME, of("" + LATITUDE),
                of(RouteDropZoneController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(RouteDropZoneController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE),
                of(RouteDropZoneController.LONGITUDE_TITLE), empty()));


        assertEquals(TOTAL_ROUTE_DROP_ZONES, collection.items.size());

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
    public void getRouteDropZoneTest(){
        ApiApplication.initApplication();
        RouteDropZoneController routeDropZoneController = new RouteDropZoneController(new RouteDropZoneRequesterImplementation(NORMAL_STATE));
        int routeId = 1, dropZoneId = 2;

        ResponseEntity res = null;
        try {
            res = routeDropZoneController.getRouteDropZone(routeId, dropZoneId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.ROUTE_DROP_ZONE_CLASS, siren._class[0]);

        //Properties
        RouteDropZone routeDropZone = (RouteDropZone) siren.properties;

        assertEquals(routeId, routeDropZone.routeId);
        assertEquals(dropZoneId, routeDropZone.dropZoneId);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(RouteDropZoneController.DELETE_ROUTE_DROP_ZONE_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.ROUTE_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(RouteDropZoneController.GET_ROUTE_DROP_ZONE_PATH.replace(RouteDropZoneController.ROUTE_ID_PATH_VAR, "" + routeId)
                .replace(RouteDropZoneController.DROP_ZONE_ID_PATH_VAR, "" + dropZoneId));
        expectedLinks.add(RouteDropZoneController.GET_ROUTE_DROP_ZONES_LIST_PATH.replace(RouteDropZoneController.ROUTE_ID_PATH_VAR, "" + routeId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }

    class RouteDropZoneRequesterImplementation implements RouteDropZoneRequester{
        public final static int
                NORMAL_STATE = 0,                 // Normal usage of the requester
                WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
                BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

        public final int implementation_state;

        private RouteDropZoneRequesterImplementation(int implementation_state) {
            this.implementation_state = implementation_state;
        }


        @Override
        public void createRouteDropZone(int routeId, int dropZoneId) throws Exception {
            if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
                throw new SQLInvalidDependencyException(); // Route or drop zone didn't exists
            }
        }
        @Override
        public void deleteRouteDropZone(int routeId, int dropZoneId) throws Exception {
            if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
                throw new SQLNonExistentEntryException(); // An entry with keys (route_id, drop_zone_id) didn't exists
            }
        }

        public static final int TOTAL_ROUTE_DROP_ZONES = 1, DROP_ZONE_ID = 1;
        public static final float LATITUDE = 0, LONGITUDE = 0;
        @Override
        public PaginatedList<RouteDropZoneWithLocation> getRouteDropZonesList(int pageNumber, int rowsPerPage, int routeId) throws Exception {
            List<RouteDropZoneWithLocation> routeDropZones = new ArrayList<>();
            routeDropZones.add(new RouteDropZoneWithLocation(routeId, DROP_ZONE_ID, LATITUDE, LONGITUDE));
            return new PaginatedList<>(TOTAL_ROUTE_DROP_ZONES, routeDropZones);
        }

        @Override
        public RouteDropZoneWithLocation getRouteDropZone(int routeId, int dropZoneId) throws Exception {
            return new RouteDropZoneWithLocation(routeId, dropZoneId, LATITUDE, LONGITUDE);
        }
    }
}
