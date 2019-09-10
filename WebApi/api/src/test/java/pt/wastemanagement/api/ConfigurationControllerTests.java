package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.ConfigurationController;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Configuration;
import pt.wastemanagement.api.requester_implementations.ConfigurationRequesterImplementation;
import pt.wastemanagement.api.views.input.ConfigurationInput;
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
import static pt.wastemanagement.api.requester_implementations.ConfigurationRequesterImplementation.*;

public class ConfigurationControllerTests {

    @Test
    public void createConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(NORMAL_STATE));
        String configurationName = "Config";
        ConfigurationInput configurationInput = new ConfigurationInput();
        configurationInput.configurationName = configurationName;

        ResponseEntity res = null;
        try {
            res = configurationController.createConfiguration(configurationInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void updateConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1;
        String configurationName = "Config";
        ConfigurationInput configurationInput = new ConfigurationInput();
        configurationInput.configurationName = configurationName;

        ResponseEntity res = null;
        try {
            res = configurationController.updateConfiguration(configurationId, configurationInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(BAD_REQUEST_STATE));
        int configurationId = 1;
        String configurationName = "Config";
        ConfigurationInput configurationInput = new ConfigurationInput();
        configurationInput.configurationName = configurationName;

        try {
            configurationController.updateConfiguration(configurationId, configurationInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void deleteConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1;

        ResponseEntity res = null;
        try {
            res = configurationController.deleteConfiguration(configurationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void deleteNonExistentConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(BAD_REQUEST_STATE));
        int configurationId = 1;

        try {
            configurationController.deleteConfiguration(configurationId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getAllConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(NORMAL_STATE));
        int page = 1, rows = 10;
        CollectionJson collection = null;

        try {
            collection = configurationController.getAllConfigurations(page, rows).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = ConfigurationController.GET_ALL_CONFIGURATIONS_PATH + "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" +
                rows;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(ConfigurationController.CONFIGURATION_NAME_FIELD_NAME, empty(),
                of(ConfigurationController.CONFIGURATION_NAME_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(ConfigurationController.CONFIGURATION_ID_FIELD_NAME, of("" + CONFIGURATION_ID),
                of(ConfigurationController.CONFIGURATION_ID_TITLE), empty()));
        expectedProperties.add(new Property(ConfigurationController.CONFIGURATION_NAME_FIELD_NAME, of(CONFIGURATION_NAME),
                of(ConfigurationController.CONFIGURATION_NAME_TITLE), empty()));

        assertEquals(TOTAL_CONFIGURATIONS, collection.items.size());

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
    public void getConfigurationTest(){
        ApiApplication.initApplication();
        ConfigurationController configurationController = new ConfigurationController(new ConfigurationRequesterImplementation(NORMAL_STATE));
        int configurationId = 1;

        ResponseEntity res = null;
        try {
            res = configurationController.getConfiguration(configurationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.CONFIGURATION_CLASS, siren._class[0]);

        //Properties
        Configuration configuration = (Configuration) siren.properties;

        assertEquals(configurationId, configuration.configurationId);
        assertEquals(CONFIGURATION_NAME, configuration.configurationName);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(ConfigurationController.UPDATE_CONFIGURATION_ACTION_NAME);
        expectedActionsNames.add(ConfigurationController.DELETE_CONFIGURATION_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.CONFIGURATION_COMMUNICATION_LIST_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(ConfigurationController.GET_CONFIGURATION_PATH.replace(ConfigurationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId));
        expectedLinks.add(ConfigurationController.GET_ALL_CONFIGURATIONS_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
