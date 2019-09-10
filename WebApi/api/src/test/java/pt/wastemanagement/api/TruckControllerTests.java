package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.TruckController;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.requester_implementations.TruckRequesterImplementation;
import pt.wastemanagement.api.views.output.GetTuck;
import pt.wastemanagement.api.views.input.TruckInput;
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
import static pt.wastemanagement.api.controllers.Controller.PAGE_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.ROWS_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.SHOW_INACTIVE_QUERY_PARAM;
import static pt.wastemanagement.api.requester_implementations.TruckRequesterImplementation.*;

public class TruckControllerTests {

    @Test
    public void createTruckTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(NORMAL_STATE));
        String plate = "AB-CD-EF";
        TruckInput truckInput = new TruckInput();
        truckInput.truckPlate = plate;

        ResponseEntity res = null;
        try {
            res = truckController.createTruck(truckInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void deactivateTruckTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(NORMAL_STATE));
        String plate = "AB-CD-EF";

        ResponseEntity res = null;
        try {
            res = truckController.deactivateTruck(plate);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void deactivateNonExistentTruckTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(BAD_REQUEST_STATE));
        String plate = "DO-NT-EX";

        try {
            truckController.deactivateTruck(plate);
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void activateTruckTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(NORMAL_STATE));
        String plate = "AB-CD-EF";

        ResponseEntity res = null;
        try {
            res = truckController.activateTruck(plate);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void activateNonExistentTruckTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(BAD_REQUEST_STATE));
        String plate = "DO-NT-EX";

        try {
            truckController.activateTruck(plate);
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getAllTrucksTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(NORMAL_STATE));
        boolean showInactive = false;
        int page = 1, rows = 20;

        CollectionJson collection = null;
        try {
            collection = truckController.getAllTrucks(page, rows, showInactive).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = TruckController.GET_ALL_TRUCKS_PATH + "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" +
                rows + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(TruckController.TRUCK_PLATE_FIELD_NAME, empty(),
                of(TruckController.TRUCK_PLATE_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(TruckController.TRUCK_PLATE_FIELD_NAME, of(TRUCK_PLATE),
                of(TruckController.TRUCK_PLATE_TITLE), empty()));
        expectedProperties.add(new Property(TruckController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)),
                of(TruckController.ACTIVE_TITLE), empty()));

        assertEquals(TOTAL_TRUCKS, collection.items.size());

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
    public void getTrucksTest(){
        ApiApplication.initApplication();
        TruckController truckController = new TruckController(new TruckRequesterImplementation(NORMAL_STATE));
        String plate = "AB-CD-EE";

        ResponseEntity res = null;
        try {
            res = truckController.getTruck(plate);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.TRUCK_CLASS, siren._class[0]);

        //Properties
        GetTuck truck = (GetTuck) siren.properties;

        assertEquals(plate, truck.registrationPlate);
        assertEquals(ACTIVE.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false), truck.active);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(TruckController.DEACTIVATE_TRUCK_ACTION_NAME);
        expectedActionsNames.add(TruckController.ACTIVATE_TRUCK_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.TRUCK_COLLECTS_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(TruckController.GET_TRUCK_PATH.replace(TruckController.TRUCK_PLATE_PATH_VAR, plate));
        expectedLinks.add(TruckController.GET_ALL_TRUCKS_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}