package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.CollectController;
import pt.wastemanagement.api.controllers.ContainerController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLWrongDateException;
import pt.wastemanagement.api.requester_implementations.CollectRequesterImplementation;
import pt.wastemanagement.api.views.input.CollectInput;
import pt.wastemanagement.api.views.output.GetCollect;
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
import static junit.framework.TestCase.*;
import static pt.wastemanagement.api.requester_implementations.CollectRequesterImplementation.*;

public class CollectControllerTests {

    @Test
    public void createCollectTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(NORMAL_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = LocalDateTime.now();

        ResponseEntity res = null;
        try {
            res = collectController.createCollect(1, collectInput);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void createCollectWithExceptionTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(WRONG_PARAMETERS_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = null;

        try {
            collectController.createCollect(1, collectInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLWrongDateException);
        }
    }

    @Test
    public void createCollectForInvalidContainerTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(BAD_REQUEST_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = null;

        try {
            collectController.createCollect(2, collectInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void collectCollectZoneContainersTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(NORMAL_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = LocalDateTime.now();

        ResponseEntity res = null;
        try {
            res = collectController.createCollect(1, collectInput);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void collectNonExistentCollectZoneContainersTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(BAD_REQUEST_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = null;

        try {
            collectController.collectCollectZoneContainers(2, collectInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateCollectTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(NORMAL_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = LocalDateTime.now();

        ResponseEntity res = null;
        try {
            res = collectController.updateCollect(1, LocalDateTime.now(), collectInput);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateCollectWithExceptionTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(WRONG_PARAMETERS_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = null;

        try {
            collectController.updateCollect(1, LocalDateTime.now(), collectInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLWrongDateException);
        }
    }

    @Test
    public void updateCollectForInvalidContainerTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(BAD_REQUEST_STATE));
        CollectInput collectInput = new CollectInput();
        collectInput.collectDate = LocalDateTime.now();

        try {
            collectController.updateCollect(2, LocalDateTime.now(), collectInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getContainerCollectsTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(NORMAL_STATE));
        int containerId = 1, page = 1, rows = 10;

        CollectionJson collection = null;

        try {
            collection = collectController.getContainerCollects(page, rows, containerId).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = CollectController.GET_CONTAINER_COLLECTS_PATH.replace(CollectController.CONTAINER_ID_PATH_VAR, "" + containerId) +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows;

        assertEquals(expectedURI,collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(CollectController.COLLECT_DATE_FIELD_NAME, empty(),of(CollectController.COLLECT_DATE_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        assertEquals(TOTAL_COLLECTS, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(CollectController.CONTAINER_ID_FIELD_NAME, of("" + containerId), of(CollectController.CONTAINER_ID_TITLE), empty()));
        expectedProperties.add(new Property(CollectController.COLLECT_DATE_FIELD_NAME, of(DATE_1),
                of(CollectController.COLLECT_DATE_TITLE), empty()));
        expectedProperties.add(new Property(CollectController.CONFIRMED_FIELD_NAME, of(CONFIRMED.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(CollectController.CONFIRMED_TITLE), empty()));

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
    public void getCollectTest(){
        ApiApplication.initApplication();
        CollectController collectController = new CollectController(new CollectRequesterImplementation(NORMAL_STATE));
        int containerId= 1;
        LocalDateTime date = LocalDateTime.now();

        ResponseEntity res = null;
        try {
            res = collectController.getCollect(containerId, date);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.COLLECT_CLASS, siren._class[0]);

        // Properties
        GetCollect collect = (GetCollect) siren.properties;

        assertEquals(containerId, collect.containerId);
        assertEquals(date, collect.collectDate);
        assertEquals(CONFIRMED.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false),
                collect.confirmed);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(CollectController.UPDATE_COLLECT_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities
        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(CollectController.GET_COLLECT_PATH
                .replace(CollectController.CONTAINER_ID_PATH_VAR, "" + containerId)
                .replace(CollectController.COLLECT_DATE_PATH_VAR, date.toString()));
        expectedLinks.add(ContainerController.GET_CONTAINER_PATH
                .replace(ContainerController.CONTAINER_ID_PATH_VAR, "" + containerId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
