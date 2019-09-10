package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.CollectZoneController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.RouteCollectionController;
import pt.wastemanagement.api.controllers.RouteController;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.requester_implementations.CollectZoneRequesterImplementation;
import pt.wastemanagement.api.views.input.CollectZoneInput;
import pt.wastemanagement.api.views.output.GetCollectZone;
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
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.requester_implementations.CollectZoneRequesterImplementation.*;


public class CollectZoneControllerTests {

    @Test
    public void createCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(NORMAL_STATE));
        int routeId= 1;

        ResponseEntity res = null;
        try {
            res = collectZoneController.createCollectZone(routeId);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void createCollectZoneOnNonExistentRouteTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(BAD_REQUEST_STATE));
        int routeId= 2;

        try {
            collectZoneController.createCollectZone(routeId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(NORMAL_STATE));
        int collectZoneId = 1, routeId = 1;
        CollectZoneInput collectZoneInput = new CollectZoneInput();
        collectZoneInput.routeId = routeId;

        ResponseEntity res = null;
        try {
            res = collectZoneController.updateCollectZone(collectZoneId, collectZoneInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(BAD_REQUEST_STATE));
        int collectZoneId = 2, routeId = 1;
        CollectZoneInput collectZoneInput = new CollectZoneInput();
        collectZoneInput.routeId = routeId;

        try {
            collectZoneController.updateCollectZone(collectZoneId, collectZoneInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void updateCollectZoneWithInvalidParametersTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int collectZoneId = 1, routeId = 2;
        CollectZoneInput collectZoneInput = new CollectZoneInput();
        collectZoneInput.routeId = routeId;

        try {
            collectZoneController.updateCollectZone(collectZoneId, collectZoneInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void activateCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(NORMAL_STATE));
        int collectZoneId = 1;

        try {
            collectZoneController.activateCollectZone(collectZoneId);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void activateNonExistentCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int collectZoneId = 2;

        try {
            collectZoneController.activateCollectZone(collectZoneId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void deactivateCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(NORMAL_STATE));
        int collectZoneId = 1;

        ResponseEntity res = null;
        try {
            res = collectZoneController.deactivateCollectZone(collectZoneId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void deactivateNonExistentCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int collectZoneId = 2;

        try {
            collectZoneController.deactivateCollectZone(collectZoneId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getRouteCollectZonesTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int routeId = 1, page = 1, rows = 10;
        boolean showInactive = false;

        CollectionJson collection = null;
        try {
            collection =  collectZoneController.getRouteCollectZones(page, rows, showInactive, routeId).collection;
        } catch (Exception e) {
            fail();
        }
        String expectedURI = CollectZoneController.GET_ROUTE_COLLECT_ZONES_PATH.replace(CollectZoneController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        assertEquals(expectedURI,collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(CollectZoneController.ROUTE_ID_FIELD_NAME, of("" + routeId), of(CollectZoneController.ROUTE_ID_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        assertEquals(TOTAL_COLLECT_ZONES, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(CollectZoneController.COLLECT_ZONE_ID_FIELD_NAME, of("" + COLLECT_ZONE_ID), of(CollectZoneController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ROUTE_ID_FIELD_NAME, of("" + routeId), of(CollectZoneController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.PICK_ORDER_FIELD_NAME, of("" + PICK_ORDER), of(CollectZoneController.PICK_ORDER_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(CollectZoneController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LATITUDE_FIELD_NAME, of("" + LATITUDE),of(CollectZoneController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE), of(CollectZoneController.LONGITUDE_TITLE), empty()));

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
    public void getCollectZonesInRangeTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int range = 50;
        float latitude = 0, longitude = 0;

        CollectionJson collection = null;
        try {
            collection =  collectZoneController.getCollectZonesInRange(latitude, longitude, range).collection;
        } catch (Exception e) {
            fail();
        }
        String expectedURI = CollectZoneController.GET_COLLECT_ZONES_IN_RANGE_PATH + "?" + CollectZoneController.LATITUDE_QUERY_PARAM + "=" + latitude
                + "&" + CollectZoneController.LONGITUDE_QUERY_PARAM + "=" + longitude + "&" + CollectZoneController.RANGE_QUERY_PARAM + "=" + range;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(CollectZoneController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID), of(CollectZoneController.ROUTE_ID_TITLE), empty()));

        assertNull(collection.template);

        assertEquals(TOTAL_COLLECT_ZONES, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(CollectZoneController.COLLECT_ZONE_ID_FIELD_NAME, of("" + COLLECT_ZONE_ID), of(CollectZoneController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID), of(CollectZoneController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.PICK_ORDER_FIELD_NAME, of("" + PICK_ORDER), of(CollectZoneController.PICK_ORDER_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(CollectZoneController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LATITUDE_FIELD_NAME, of("" + LATITUDE),of(CollectZoneController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE), of(CollectZoneController.LONGITUDE_TITLE), empty()));

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

    /**
     * This test is here instead of being in RouteCollectionController, because the dummy implementation was build in this class.
     */
    @Test
    public void getRouteCollectionPlanTest(){
        ApiApplication.initApplication();
        RouteCollectionController collectZoneController =
                new RouteCollectionController(null, null, new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int page = 1, rows = 10, routeId = 1;
        String containerType = "general";

        CollectionJson collection = null;
        try {
            collection =  collectZoneController.getRouteCollectionPlan(page, rows, routeId, containerType).collection;
        } catch (Exception e) {
            fail();
        }
        String expectedURI = RouteCollectionController.GET_ROUTE_COLLECTION_PLAN_PATH.replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows;
        assertEquals(expectedURI, collection.href.toString());

        assertNull(collection.template);

        assertEquals(TOTAL_COLLECT_ZONES, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(CollectZoneController.COLLECT_ZONE_ID_FIELD_NAME, of("" + COLLECT_ZONE_ID), of(CollectZoneController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ROUTE_ID_FIELD_NAME, of("" + ROUTE_ID), of(CollectZoneController.ROUTE_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.PICK_ORDER_FIELD_NAME, of("" + PICK_ORDER), of(CollectZoneController.PICK_ORDER_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(CollectZoneController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LATITUDE_FIELD_NAME, of("" + LATITUDE),of(CollectZoneController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(CollectZoneController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE), of(CollectZoneController.LONGITUDE_TITLE), empty()));

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
    public void getCollectZoneTest(){
        ApiApplication.initApplication();
        CollectZoneController collectZoneController = new CollectZoneController(new CollectZoneRequesterImplementation(WRONG_PARAMETERS_STATE));
        int collectZoneId = 1;

        ResponseEntity res = null;
        try {
            res = collectZoneController.getCollectZone(collectZoneId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.COLLECT_ZONE_CLASS, siren._class[0]);

        // Properties
        GetCollectZone collectZone = (GetCollectZone) siren.properties;

        assertEquals(collectZoneId, collectZone.collectZoneId);
        assertEquals(ACTIVE.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false), collectZone.active);
        assertEquals(LATITUDE, collectZone.latitude);
        assertEquals(LONGITUDE, collectZone.longitude);
        assertEquals(TOTAL_COLLECT_ZONES, collectZone.numContainers);
        assertEquals(PICK_ORDER, collectZone.pickOrder);
        assertEquals(ROUTE_ID, collectZone.routeId);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(CollectZoneController.UPDATE_COLLECT_ZONE_ACTION_NAME);
        expectedActionsNames.add(CollectZoneController.DEACTIVATE_COLLECT_ZONE_ACTION_NAME);
        expectedActionsNames.add(CollectZoneController.ACTIVATE_COLLECT_ZONE_ACTION_NAME);


        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities
        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(COLLECT_ZONE_CONTAINERS_CLASS);
        expectedEntitiesClasses.add(COLLECTION_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(CollectZoneController.GET_COLLECT_ZONE_PATH.replace(CollectZoneController.COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId));
        expectedLinks.add(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + ROUTE_ID));
        expectedLinks.add(RouteController.GET_ALL_ROUTES_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
