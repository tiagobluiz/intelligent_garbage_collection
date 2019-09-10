package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.StationController;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.mappers.StationMapper;
import pt.wastemanagement.api.model.Station;
import pt.wastemanagement.api.requester_implementations.StationRequesterImplementation;
import pt.wastemanagement.api.views.input.StationInput;
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
import static pt.wastemanagement.api.controllers.Controller.PAGE_QUERY_PARAM;
import static pt.wastemanagement.api.controllers.Controller.ROWS_QUERY_PARAM;
import static pt.wastemanagement.api.requester_implementations.StationRequesterImplementation.*;

public class StationControllerTests {

    @Test
    public void createStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(NORMAL_STATE));

        float latitude = 0, longitude = 0;
        String stationName = "this is a test", stationType = "base";
        StationInput stationInput = new StationInput();
        stationInput.stationType = stationType;
        stationInput.latitude = latitude;
        stationInput.longitude = longitude;
        stationInput.stationName = stationName;

        ResponseEntity res = null;
        try {
            res = stationController.createStation(stationInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());

        assertEquals(StationController.GET_STATION_PATH.replace(StationController.STATION_ID_PATH_VAR, "" + STATION_ID),
                res.getHeaders().get(HttpHeaders.LOCATION).get(0));
    }

    @Test
    public void updateStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(NORMAL_STATE));

        int stationId = 1;
        float latitude = 0, longitude = 0;
        String stationName = "this is a test", stationType = "base";
        StationInput stationInput = new StationInput();
        stationInput.stationType = stationType;
        stationInput.latitude = latitude;
        stationInput.longitude = longitude;
        stationInput.stationName = stationName;

        ResponseEntity res = null;
        try {
            res = stationController.updateStation(stationId, stationInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void updateNonExistentStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(BAD_REQUEST_STATE));

        int stationId = 2;
        float latitude = 0, longitude = 0;
        String stationName = "this is a test", stationType = "base";
        StationInput stationInput = new StationInput();
        stationInput.stationType = stationType;
        stationInput.latitude = latitude;
        stationInput.longitude = longitude;
        stationInput.stationName = stationName;

        try {
            stationController.updateStation(stationId, stationInput);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }

    }

    @Test
    public void deleteStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(NORMAL_STATE));

        int stationId = 1;

        ResponseEntity res = null;
        try {
            res = stationController.deleteStation(stationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void deleteNonExistentStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(BAD_REQUEST_STATE));

        int stationId = 1;

        try {
            stationController.deleteStation(stationId);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof SQLNonExistentEntryException);
        }
    }

    @Test
    public void getAllStationsTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(NORMAL_STATE));

        int page = 1, rows = 10;
        CollectionJson collection = null;

        try {
            collection = stationController.getAllStations(page, rows).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = StationController.GET_ALL_STATIONS_PATH + "?" + PAGE_QUERY_PARAM + "=" + page + "&" + ROWS_QUERY_PARAM + "=" + rows;
        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(StationController.STATION_NAME_FIELD_NAME, empty(),
                of(StationController.STATION_NAME_TITLE), empty()));
        expectedTemplateProperties.add(new Property(StationController.LATITUDE_FIELD_NAME, empty(),
                of(StationController.LATITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(StationController.LONGITUDE_FIELD_NAME, empty(),
                of(StationController.LONGITUDE_TITLE), empty()));
        expectedTemplateProperties.add(new Property(StationController.STATION_TYPE_FIELD_NAME, empty(),
                of(StationController.STATION_TYPE_TITLE), of(StationMapper.STATION_TYPES)));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(StationController.STATION_ID_FIELD_NAME, of("" + STATION_ID),
                of(StationController.STATION_ID_TITLE), empty()));
        expectedProperties.add(new Property(StationController.STATION_NAME_FIELD_NAME, of(STATION_NAME),
                of(StationController.STATION_NAME_TITLE), empty()));
        expectedProperties.add(new Property(StationController.LATITUDE_FIELD_NAME, of("" + LATITUDE),
                of(StationController.LATITUDE_TITLE), empty()));
        expectedProperties.add(new Property(StationController.LONGITUDE_FIELD_NAME, of("" + LONGITUDE),
                of(StationController.LONGITUDE_TITLE), empty()));
        expectedProperties.add(new Property(StationController.STATION_TYPE_FIELD_NAME, of(STATION_TYPE),
                of(StationController.STATION_TYPE_TITLE), empty()));

        assertEquals(TOTAL_STATIONS, collection.items.size());

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
    public void getStationTest(){
        ApiApplication.initApplication();
        StationController stationController = new StationController(new StationRequesterImplementation(NORMAL_STATE));

        int stationId = 1;

        ResponseEntity res = null;
        try {
            res = stationController.getStation(stationId);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
        SirenOutput siren  = (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.STATION_CLASS, siren._class[0]);

        //Properties
        Station station = (Station) siren.properties;

        assertEquals(stationId, station.stationId);
        assertEquals(STATION_NAME, station.stationName);
        assertEquals(LATITUDE, station.latitude);
        assertEquals(LONGITUDE, station.longitude);
        assertEquals(STATION_TYPE, station.stationType);



        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(StationController.UPDATE_STATION_ACTION_NAME);
        expectedActionsNames.add(StationController.DELETE_STATION_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(StationController.GET_STATION_PATH.replace(StationController.STATION_ID_PATH_VAR, "" + stationId));
        expectedLinks.add(StationController.GET_ALL_STATIONS_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}