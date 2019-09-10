package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.CommunicationController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.exceptions.SQLDependencyBreakException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Communication;
import pt.wastemanagement.api.requester_implementations.CommunicationRequesterImplementation;
import pt.wastemanagement.api.views.input.CommunicationInput;
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
import static pt.wastemanagement.api.requester_implementations.CommunicationRequesterImplementation.*;

public class CommunicationControllerTests {

    @Test
    public void createCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController = new CommunicationController(new CommunicationRequesterImplementation(NORMAL_STATE));
        String communicationName = "Com";
        CommunicationInput communicationInput = new CommunicationInput();
        communicationInput.communicationDesignation = communicationName;

        ResponseEntity res = null;
        try {
            res = communicationController.createCommunication(communicationInput);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void updateCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController = new CommunicationController(new CommunicationRequesterImplementation(NORMAL_STATE));
        int communicationId = 1;
        String communicationName = "Com";
        CommunicationInput communicationInput = new CommunicationInput();
        communicationInput.communicationDesignation = communicationName;

        ResponseEntity res = null;
        try {
            res = communicationController.updateCommunication(communicationId, communicationInput);
        } catch (Exception e) {
            fail();
        }
        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(BAD_REQUEST_STATE));
        int communicationId = 1;
        String communicationName = "Com";
        CommunicationInput communicationInput = new CommunicationInput();
        communicationInput.communicationDesignation = communicationName;

        try {
            communicationController.updateCommunication(communicationId, communicationInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void deleteCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(NORMAL_STATE));
        int communicationId = 1;

        ResponseEntity res = null;
        try {
            res = communicationController.deleteCommunication(communicationId);
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void deleteStillInUseCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(WRONG_PARAMETERS_STATE));
        int communicationId = 1;

        try {
            communicationController.deleteCommunication(communicationId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLDependencyBreakException);
        }
    }

    @Test
    public void deleteNonExistentCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(BAD_REQUEST_STATE));
        int communicationId = 1;

        try {
            communicationController.deleteCommunication(communicationId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getAllCommunicationsTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(NORMAL_STATE));
        int page = 1, rows = 10;

        CollectionJson collection = null;

        try {
            collection = communicationController.getAllCommunications(page, rows).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = CommunicationController.GET_ALL_COMMUNICATIONS_PATH +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(CommunicationController.COMMUNICATION_NAME_FIELD_NAME, empty(),
                of(CommunicationController.COMMUNICATION_NAME_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(CommunicationController.COMMUNICATION_ID_FIELD_NAME, of("" + COMMUNICATION_ID),
                of(CommunicationController.COMMUNICATION_ID_TITLE), empty()));
        expectedProperties.add(new Property(CommunicationController.COMMUNICATION_NAME_FIELD_NAME, of(COMMUNICATION_NAME),
                of(CommunicationController.COMMUNICATION_NAME_TITLE), empty()));

        assertEquals(TOTAL_COMMUNICATIONS, collection.items.size());

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
    public void getCommunicationTest(){
        ApiApplication.initApplication();
        CommunicationController communicationController =
                new CommunicationController(new CommunicationRequesterImplementation(NORMAL_STATE));
        int communicationId = 1;
        ResponseEntity res = null;

        try {
            res = communicationController.getCommunication(communicationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.COMMUNICATION_CLASS, siren._class[0]);

        //Properties
        Communication communication = (Communication) siren.properties;

        assertEquals(communicationId, communication.communicationId);
        assertEquals(COMMUNICATION_NAME, communication.communicationDesignation);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(CommunicationController.UPDATE_COMMUNICATION_ACTION_NAME);
        expectedActionsNames.add(CommunicationController.DELETE_COMMUNICATION_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(CommunicationController.GET_COMMUNICATION_PATH.replace(CommunicationController.COMMUNICATION_ID_PATH_VAR, "" + communicationId));
        expectedLinks.add(CommunicationController.GET_ALL_COMMUNICATIONS_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
