package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.ConfigurationCommunicationController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.requester_implementations.ConfigurationCommunicationRequesterImplementation;
import pt.wastemanagement.api.views.input.ConfigurationCommunicationInput;
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
import static pt.wastemanagement.api.requester_implementations.ConfigurationCommunicationRequesterImplementation.*;

public class ConfigurationCommunicationControllerTests {

    @Test
    public void associateCommunicationToTheConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1, communicationId = 1, value = 70;
        ConfigurationCommunicationInput configurationCommunicationInput = new ConfigurationCommunicationInput();
        configurationCommunicationInput.communicationId = communicationId;
        configurationCommunicationInput.value = value;

        ResponseEntity res = null;
        try {
            res = configurationCommunicationController.associateCommunicationToTheConfiguration(configurationId, configurationCommunicationInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void associateNonExistentCommunicationToTheConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(BAD_REQUEST_STATE));
        int configurationId = 1, communicationId = 2, value = 70;
        ConfigurationCommunicationInput configurationCommunicationInput = new ConfigurationCommunicationInput();
        configurationCommunicationInput.communicationId = communicationId;
        configurationCommunicationInput.value = value;

        try {
            configurationCommunicationController.associateCommunicationToTheConfiguration(configurationId, configurationCommunicationInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void disassociateCommunicationToTheConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1, communicationId = 1;

        ResponseEntity res = null;
        try {
            res = configurationCommunicationController.disassociateCommunicationToTheConfiguration(configurationId, communicationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void disassociateNonExistentCommunicationConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(BAD_REQUEST_STATE));
        int configurationId = 1, communicationId = 2;

        try {
            configurationCommunicationController.disassociateCommunicationToTheConfiguration(configurationId, communicationId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getConfigurationCommunicationListTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1, page = 1, rows = 10;

        CollectionJson collection = null;

        try {
            collection = configurationCommunicationController.getConfigurationCommunicationList(page, rows, configurationId).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = ConfigurationCommunicationController.GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH
                .replace(ConfigurationCommunicationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId) +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows ;

        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(ConfigurationCommunicationController.COMMUNICATION_ID_FIELD_NAME, empty(),
                of(ConfigurationCommunicationController.COMMUNICATION_ID_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ConfigurationCommunicationController.VALUE_FIELD_NAME, empty(),
                of(ConfigurationCommunicationController.VALUE_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(ConfigurationCommunicationController.COMMUNICATION_ID_FIELD_NAME, of("" + COMMUNICATION_ID),
                of(ConfigurationCommunicationController.COMMUNICATION_ID_TITLE), empty()));
        expectedProperties.add(new Property(ConfigurationCommunicationController.CONFIGURATION_ID_FIELD_NAME, of("" + configurationId),
                of(ConfigurationCommunicationController.CONFIGURATION_ID_TITLE), empty()));
        expectedProperties.add(new Property(ConfigurationCommunicationController.VALUE_FIELD_NAME, of("" + VALUE),
                of(ConfigurationCommunicationController.VALUE_TITLE), empty()));
        expectedProperties.add(new Property(ConfigurationCommunicationController.COMMUNICATION_NAME_FIELD_NAME, of(COMM_NAME),
                of(ConfigurationCommunicationController.COMMUNICATION_NAME_TITLE), empty()));

        assertEquals(TOTAL_CONFIGURATION_COMMUNICATIONS, collection.items.size());

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
    public void getConfigurationCommunicationTest(){
        ApiApplication.initApplication();
        ConfigurationCommunicationController configurationCommunicationController =
                new ConfigurationCommunicationController(new ConfigurationCommunicationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1, communicationId = 1;

        ResponseEntity res = null;
        try {
            res = configurationCommunicationController.getConfigurationCommunication(configurationId, communicationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.CONFIGURATION_COMMUNICATION_CLASS, siren._class[0]);

        //Properties
        ConfigurationCommunication configurationCommunication = (ConfigurationCommunication) siren.properties;

        assertEquals(configurationId, configurationCommunication.configurationId);
        assertEquals(communicationId, configurationCommunication.communicationId);
        assertEquals(VALUE, configurationCommunication.value);
        assertEquals(COMM_NAME, configurationCommunication.communicationDesignation);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(ConfigurationCommunicationController.DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.COMMUNICATION_CLASS);
        expectedEntitiesClasses.add(Controller.CONFIGURATION_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(ConfigurationCommunicationController.GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH
                .replace(ConfigurationCommunicationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId));
        expectedLinks.add(ConfigurationCommunicationController.GET_CONFIGURATION_COMMUNICATION_PATH
                .replace(ConfigurationCommunicationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId)
                .replace(ConfigurationCommunicationController.COMMUNICATION_ID_PATH_VAR, "" + communicationId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
