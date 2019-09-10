package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.ContainerController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.WashController;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Wash;
import pt.wastemanagement.api.requester_implementations.WashRequesterImplementation;
import pt.wastemanagement.api.views.input.WashInput;
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
import static org.junit.Assert.fail;
import static pt.wastemanagement.api.requester_implementations.WashRequesterImplementation.*;

public class WashControllerTests {

    @Test
    public void createWashTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(NORMAL_STATE));
        int containerId = 1;
        LocalDateTime date = LocalDateTime.now();
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        ResponseEntity res = null;
        try {
            res = washController.createWash(containerId, washInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void  createWashForNonExistentContainerTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(BAD_REQUEST_STATE));
        int containerId = 2;
        LocalDateTime date = LocalDateTime.now();
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        try {
            washController.createWash(containerId, washInput);
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void washCollectZoneContainersTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(NORMAL_STATE));
        int collectZoneId = 1;
        LocalDateTime date = LocalDateTime.now();
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        ResponseEntity res = null;
        try {
            res = washController.washCollectZoneContainers(collectZoneId, washInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void  washNonExistentCollectZoneContainersTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(BAD_REQUEST_STATE));
        int collectZoneId = 2;
        LocalDateTime date = LocalDateTime.now();
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        try {
            washController.washCollectZoneContainers(collectZoneId, washInput);
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateWashTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(NORMAL_STATE));
        int containerId = 1;
        LocalDateTime date = LocalDateTime.parse("2018-07-14T23:59:59");
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        ResponseEntity res = null;
        try {
            res = washController.updateWash(containerId, LocalDateTime.now(), washInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentWashTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(BAD_REQUEST_STATE));
        int containerId = 2;
        LocalDateTime date = LocalDateTime.parse("2018-07-14T23:59:59");
        WashInput washInput = new WashInput();
        washInput.washDate = date;

        try {
            washController.updateWash(containerId, LocalDateTime.now(), washInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getContainerWashesTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(NORMAL_STATE));
        int containerId = 1, page = 1, rows = 20;

        CollectionJson collection = null;
        try {
            collection = washController.getContainerWashes(page, rows, containerId).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = WashController.GET_CONTAINER_WASHES_PATH
                .replace(WashController.CONTAINER_ID_PATH_VAR, "" + containerId) +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows ;

        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(WashController.WASH_DATE_FIELD_NAME, empty(),
                of(WashController.WASH_DATE_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(WashController.CONTAINER_ID_FIELD_NAME, of("" + containerId),
                of(WashController.CONTAINER_ID_TITLE), empty()));
        expectedProperties.add(new Property(WashController.WASH_DATE_FIELD_NAME, of(WASH_DATE),
                of(WashController.WASH_DATE_TITLE), empty()));

        assertEquals(TOTAL_WASHES, collection.items.size());

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
    public void getWashTest(){
        ApiApplication.initApplication();
        WashController washController = new WashController(new WashRequesterImplementation(NORMAL_STATE));
        int containerId = 1;
        LocalDateTime date = LocalDateTime.now();

       ResponseEntity res = null;
        try {
            res = washController.getWash(containerId, date);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.WASH_CLASS, siren._class[0]);

        //Properties
        Wash wash = (Wash) siren.properties;

        assertEquals(containerId, wash.containerId);
        assertEquals(date.toString(), wash.washDate.toString());

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(WashController.UPDATE_WASH_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(WashController.GET_WASH_PATH
                .replace(WashController.CONTAINER_ID_PATH_VAR, "" + containerId)
                .replace(WashController.WASH_DATE_PATH_VAR, date.toString()));
        expectedLinks.add(WashController.GET_CONTAINER_WASHES_PATH
                .replace(ContainerController.CONTAINER_ID_PATH_VAR, "" + containerId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}