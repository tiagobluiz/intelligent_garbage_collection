package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.*;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.mappers.ContainerMapper;
import pt.wastemanagement.api.requester_implementations.ConfigurationCommunicationRequesterImplementation;
import pt.wastemanagement.api.requester_implementations.ContainerRequesterImplementation;
import pt.wastemanagement.api.views.input.ContainerConfigurationInput;
import pt.wastemanagement.api.views.input.ContainerInput;
import pt.wastemanagement.api.views.input.ContainerLocalizationInput;
import pt.wastemanagement.api.views.input.ContainerReadsInput;
import pt.wastemanagement.api.views.output.GetContainer;
import pt.wastemanagement.api.views.output.OccupationInRangeReturn;
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
import static pt.wastemanagement.api.requester_implementations.ContainerRequesterImplementation.*;

public class ContainerControllerTests {

    @Test
    public void createContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int collectZoneId = 1, configurationId = 1;
        short height = 1;
        String containerType = "general", iotId = "1";
        float latitude = 0, longitude = 0;
        ContainerInput containerInput = new ContainerInput();
        containerInput.configurationId = configurationId;
        containerInput.containerType = containerType;
        containerInput.iotId = iotId;
        containerInput.latitude = latitude;
        containerInput.longitude = longitude;
        containerInput.height = height;

        ResponseEntity res = null;
        try {
            res = containerController.createContainer(collectZoneId, containerInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
    }

    @Test
    public void createContainerForNonExistentCollectZoneTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        int collectZoneId = 2, configurationId = 1;
        String containerType = "general", iotId = "1";
        float latitude = 0, longitude = 0;
        ContainerInput containerInput = new ContainerInput();
        containerInput.configurationId = configurationId;
        containerInput.containerType = containerType;
        containerInput.iotId = iotId;
        containerInput.latitude = latitude;
        containerInput.longitude = longitude;
        containerInput.height = 250;

        try {
            containerController.createContainer(collectZoneId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateContainerConfigurationsTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int collectZoneId = 1, configurationId = 1;
        String containerType = "general", iotId = "1";
        ContainerConfigurationInput containerInput = new ContainerConfigurationInput();
        containerInput.configurationId = configurationId;
        containerInput.containerType = containerType;
        containerInput.iotId = iotId;
        containerInput.height = 250;

        ResponseEntity res = null;
        try {
            res = containerController.updateContainerConfiguration(collectZoneId, containerInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentContainerConfigurationsTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        int collectZoneId = 1, configurationId = 1;
        String containerType = "general", iotId = "1";
        ContainerConfigurationInput containerInput = new ContainerConfigurationInput();
        containerInput.configurationId = configurationId;
        containerInput.containerType = containerType;
        containerInput.iotId = iotId;
        containerInput.height = 250;

        try {
            containerController.updateContainerConfiguration(collectZoneId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void updateContainerConfigurationsWithNonExistentConfigurationTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(WRONG_PARAMETERS_STATE), null);
        int collectZoneId = 1, configurationId = 2;
        String containerType = "general", iotId = "1";
        ContainerConfigurationInput containerInput = new ContainerConfigurationInput();
        containerInput.configurationId = configurationId;
        containerInput.containerType = containerType;
        containerInput.iotId = iotId;
        containerInput.height = 250;

        try {
            containerController.updateContainerConfiguration(collectZoneId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateContainerLocalizationTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int collectZoneId = 1;
        float latitude = 0, longitude = 0;
        ContainerLocalizationInput containerInput = new ContainerLocalizationInput();
        containerInput.collectZoneId = collectZoneId;
        containerInput.latitude = latitude;
        containerInput.longitude = longitude;

        ResponseEntity res = null;
        try {
            res = containerController.updateContainerLocalization(collectZoneId, containerInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentContainerLocalizationTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        int collectZoneId = 1;
        float latitude = 0, longitude = 0;
        ContainerLocalizationInput containerInput = new ContainerLocalizationInput();
        containerInput.collectZoneId = collectZoneId;
        containerInput.latitude = latitude;
        containerInput.longitude = longitude;

        try {
            containerController.updateContainerLocalization(collectZoneId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);;
        }
    }

    @Test
    public void updateContainerConfigurationsWithNonExistentCollectZoneTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(WRONG_PARAMETERS_STATE), null);
        int collectZoneId = 2;
        float latitude = 0, longitude = 0;
        ContainerLocalizationInput containerInput = new ContainerLocalizationInput();
        containerInput.collectZoneId = collectZoneId;
        containerInput.latitude = latitude;
        containerInput.longitude = longitude;

        try {
            containerController.updateContainerLocalization(collectZoneId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLInvalidDependencyException);
        }
    }

    @Test
    public void updateContainerReadsTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE),
                new ConfigurationCommunicationRequesterImplementation(ConfigurationCommunicationRequesterImplementation.NORMAL_STATE));
        String iotId = "1";
        short battery = 90, occupation = 70, temperature = 25;
        ContainerReadsInput containerInput = new ContainerReadsInput();
        containerInput.battery = battery;
        containerInput.occupation = occupation;
        containerInput.temperature = temperature;

        ResponseEntity res = null;
        try {
            res = containerController.updateContainerReads(iotId, containerInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentContainerReadsTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        String iotId = "2";
        short battery = 90, occupation = 70, temperature = 25;
        ContainerReadsInput containerInput = new ContainerReadsInput();
        containerInput.battery = battery;
        containerInput.occupation = occupation;
        containerInput.temperature = temperature;

        try {
            containerController.updateContainerReads(iotId, containerInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void activateContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int containerId = 1;

        ResponseEntity res = null;
        try {
            res = containerController.activateContainer(containerId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());

    }

    @Test
    public void activateNonExistentContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        int containerId = 2;

        try {
            containerController.activateContainer(containerId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void deactivateContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int containerId = 1;

        ResponseEntity res = null;
        try {
            res = containerController.deactivateContainer(containerId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());

    }

    @Test
    public void deactivateNonExistentContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(BAD_REQUEST_STATE), null);
        int containerId = 2;

        try {
            containerController.deactivateContainer(containerId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getCollectZoneContainersTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int collectZoneId = 1, page = 1, rows = 10;
        boolean showInactive = true;

        CollectionJson collection = null;
        try {
            collection =  containerController.getCollectZoneContainers(page, rows, showInactive, collectZoneId).collection;
        } catch (Exception e) {
            fail();
        }
        String expectedURI = ContainerController.GET_COLLECT_ZONE_CONTAINERS_PATH.replace(ContainerController.COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId) +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows + "&" + Controller.SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        assertEquals(expectedURI,collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(ContainerController.IOT_ID_FIELD_NAME, empty(), of(ContainerController.IOT_ID_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ContainerController.LATITUDE_FIELD_NAME, empty(), of(ContainerController.LATITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ContainerController.LONGITUDE_FIELD_NAME, empty(), of(ContainerController.LONGITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ContainerController.HEIGHT_FIELD_NAME, empty(), of(ContainerController.HEIGHT_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ContainerController.CONTAINER_TYPE_FIELD_NAME, empty(), of(ContainerController.CONTAINER_TYPE_TITLE),
                of(ContainerMapper.CONTAINER_TYPES)));
        expectedTemplateProperties.add(new Property(ContainerController.COLLECT_ZONE_ID_FIELD_NAME, empty(), of(ContainerController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedTemplateProperties.add(new Property(ContainerController.CONFIGURATION_ID_FIELD_NAME, empty(),of(ContainerController.CONFIGURATION_ID_TITLE), empty()));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        assertEquals(TOTAL_CONTAINERS, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(ContainerController.CONTAINER_ID_FIELD_NAME, of("" + CONTAINER_ID), of(ContainerController.CONTAINER_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.IOT_ID_FIELD_NAME, of(IOT_ID), of(ContainerController.IOT_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(ContainerController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LATITUDE_FIELD_NAME, of("" + LATITUDE), of(ContainerController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE), of(ContainerController.LONGITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.HEIGHT_FIELD_NAME, of("" + HEIGHT), of(ContainerController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LAST_READ_DATE_FIELD_NAME, of(LAST_READ_DATE), of(ContainerController.LAST_READ_DATE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.CONTAINER_TYPE_FIELD_NAME, of(CONTAINER_TYPE), of(ContainerController.CONTAINER_TYPE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.BATTERY_FIELD_NAME, of("" + BATTERY), of(ContainerController.BATTERY_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.OCCUPATION_FIELD_NAME, of("" + OCCUPATION), of(ContainerController.OCCUPATION_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.TEMPERATURE_FIELD_NAME, of("" + TEMPERATURE), of(ContainerController.TEMPERATURE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.COLLECT_ZONE_ID_FIELD_NAME, of("" + COLLECT_ZONE_ID), of(ContainerController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.CONFIGURATION_ID_FIELD_NAME, of("" + CONFIGURATION_ID),of(ContainerController.CONFIGURATION_ID_TITLE), empty()));

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
    public void getRouteContainersTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int routeId = 1, page = 1, rows = 10;
        boolean showInactive = true;

        CollectionJson collection = null;
        try {
            collection =  containerController.getRouteContainers(page, rows, showInactive, routeId).collection;
        } catch (Exception e) {
            fail();
        }
        String expectedURI = ContainerController.GET_ROUTE_CONTAINERS_PATH.replace(ContainerController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows + "&" + Controller.SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        assertEquals(expectedURI, collection.href.toString());

        assertNull(collection.template);

        assertEquals(TOTAL_CONTAINERS, collection.items.size());

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(ContainerController.CONTAINER_ID_FIELD_NAME, of("" + CONTAINER_ID), of(ContainerController.CONTAINER_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.IOT_ID_FIELD_NAME, of(IOT_ID), of(ContainerController.IOT_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.ACTIVE_FIELD_NAME, of(ACTIVE.equalsIgnoreCase("T")
                ? Boolean.toString(true) : Boolean.toString(false)), of(ContainerController.ACTIVE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LATITUDE_FIELD_NAME, of("" + LATITUDE), of(ContainerController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE), of(ContainerController.LONGITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.HEIGHT_FIELD_NAME, of("" + HEIGHT), of(ContainerController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.LAST_READ_DATE_FIELD_NAME, of(LAST_READ_DATE), of(ContainerController.LAST_READ_DATE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.CONTAINER_TYPE_FIELD_NAME, of(CONTAINER_TYPE), of(ContainerController.CONTAINER_TYPE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.BATTERY_FIELD_NAME, of("" + BATTERY), of(ContainerController.BATTERY_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.OCCUPATION_FIELD_NAME, of("" + OCCUPATION), of(ContainerController.OCCUPATION_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.TEMPERATURE_FIELD_NAME, of("" + TEMPERATURE), of(ContainerController.TEMPERATURE_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.COLLECT_ZONE_ID_FIELD_NAME, of("" + COLLECT_ZONE_ID), of(ContainerController.COLLECT_ZONE_ID_TITLE), empty()));
        expectedProperties.add(new Property(ContainerController.CONFIGURATION_ID_FIELD_NAME, of("" + CONFIGURATION_ID),of(ContainerController.CONFIGURATION_ID_TITLE), empty()));

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
    public void getContainerTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int containerId = 1;

        ResponseEntity res = null;
        try {
            res = containerController.getContainer(containerId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.CONTAINER_CLASS, siren._class[0]);

        //Properties
        GetContainer container = (GetContainer) siren.properties;

        assertEquals(containerId, container.containerId);
        assertEquals(IOT_ID, container.iotId);
        assertEquals(ACTIVE.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false), container.active);
        assertEquals(LATITUDE, container.latitude);
        assertEquals(LONGITUDE, container.longitude);
        assertEquals(HEIGHT, container.height);
        assertEquals(CONTAINER_TYPE, container.containerType);
        assertEquals(BATTERY, container.battery);
        assertEquals(OCCUPATION, container.occupation);
        assertEquals(TEMPERATURE, container.temperature);
        assertEquals(COLLECT_ZONE_ID, container.collectZoneId);
        assertEquals(CONFIGURATION_ID, container.configurationId);
        assertEquals(NUM_COLLECTS, container.numCollects);
        assertEquals(NUM_WASHES, container.numWashes);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(ContainerController.UPDATE_CONTAINER_CONFIGURATION_ACTION_NAME);
        expectedActionsNames.add(ContainerController.UPDATE_CONTAINER_LOCALIZATION_ACTION_NAME);
        expectedActionsNames.add(ContainerController.UPDATE_CONTAINER_READS_ACTION_NAME);
        expectedActionsNames.add(ContainerController.DEACTIVATE_CONTAINER_ACTION_NAME);
        expectedActionsNames.add(ContainerController.ACTIVATE_CONTAINER_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        List<String> expectedEntitiesClasses = new ArrayList<>();
        expectedEntitiesClasses.add(Controller.CONFIGURATION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECT_ZONE_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECT_CLASS);
        expectedEntitiesClasses.add(Controller.WASH_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);
        expectedEntitiesClasses.add(Controller.COLLECTION_CLASS);

        siren.entities.forEach(entity -> {
            for (int i = 0; i < entity._class.length; i++) {
                assertTrue(expectedEntitiesClasses.contains(entity._class[i]));
                expectedEntitiesClasses.remove(entity._class[i]);
            }
        });

        assertEquals(0, expectedEntitiesClasses.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(ContainerController.GET_CONTAINER_PATH.replace(ContainerController.CONTAINER_ID_PATH_VAR, "" + containerId));
        expectedLinks.add(ConfigurationController.GET_ALL_CONFIGURATIONS_PATH);
        expectedLinks.add(CollectZoneController.GET_COLLECT_ZONES_IN_RANGE_PATH+ "?" + CollectZoneController.LATITUDE_QUERY_PARAM + "=" + container.latitude + "&" +
                CollectZoneController.LONGITUDE_QUERY_PARAM + "=" + container.longitude);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }

    @Test
    public void getContainersWithOccupationBetweenRangeTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int min = 0, max = 100;

        ResponseEntity res = null;
        try {
            res = containerController.getContainersWithOccupationInRange(min, max);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.CONTAINERS_IN_RANGE_CLASS, siren._class[0]);

        //Properties
        OccupationInRangeReturn range = (OccupationInRangeReturn) siren.properties;

        assertEquals(CONTAINERS_IN_RANGE_RETURN, range.occupationInRange);

        //Actions
        assertEquals(0, siren.actions.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(ContainerController.GET_CONTAINERS_IN_RANGE_PATH +
                "?" + ContainerController.MIN_RANGE_QUERY_PARAM + "=" + min + "&" + ContainerController.MAX_RANGE_QUERY_PARAM + "=" + max);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }

    @Test
    public void getContainerOfARoutesWithOccupationBetweenRangeTest(){
        ApiApplication.initApplication();
        ContainerController containerController = new ContainerController(new ContainerRequesterImplementation(NORMAL_STATE), null);
        int min = 0, max = 100, routeId = 1;

        ResponseEntity res = null;
        try {
            res = containerController.getContainersOfARouteWithOccupationInRange(routeId, min, max);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.CONTAINERS_IN_RANGE_CLASS, siren._class[0]);

        //Properties
        OccupationInRangeReturn range = (OccupationInRangeReturn) siren.properties;

        assertEquals(CONTAINERS_IN_RANGE_RETURN, range.occupationInRange);

        //Actions
        assertEquals(0, siren.actions.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(ContainerController.GET_ROUTE_CONTAINERS_IN_RANGE_PATH.replace(ContainerController.ROUTE_ID_PATH_VAR, "" + routeId) +
                "?" + ContainerController.MIN_RANGE_QUERY_PARAM + "=" + min + "&" + ContainerController.MAX_RANGE_QUERY_PARAM + "=" + max);
        expectedLinks.add(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId));

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}