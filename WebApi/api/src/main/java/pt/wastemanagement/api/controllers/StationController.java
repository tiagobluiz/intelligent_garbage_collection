package pt.wastemanagement.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.mappers.StationMapper;
import pt.wastemanagement.api.model.Station;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.StationRequester;
import pt.wastemanagement.api.views.input.StationInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class StationController  {
    //Field
    public static final String
            STATION_ID_FIELD_NAME = "stationId",
            STATION_NAME_FIELD_NAME = "stationName",
            LATITUDE_FIELD_NAME = "latitude",
            LONGITUDE_FIELD_NAME = "longitude",
            STATION_TYPE_FIELD_NAME = "stationType";

    //Field Titles
    public static final String
            STATION_ID_TITLE = "Station Id",
            STATION_NAME_TITLE = "Station Name",
            LATITUDE_TITLE = "Latitude",
            LONGITUDE_TITLE = "Longitude",
            STATION_TYPE_TITLE = "Station Type";

    //Path Vars
    public static final String
            STATION_ID_PATH_VAR_NAME = "station_id",
            STATION_ID_PATH_VAR = "{" + STATION_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            STATIONS_PREFIX = "/stations";

    //Paths
    public static final String
    // /stations
            CREATE_STATION_PATH = STATIONS_PREFIX,
            GET_ALL_STATIONS_PATH = CREATE_STATION_PATH,
    // /stations/{station_id}
            GET_STATION_PATH = CREATE_STATION_PATH + "/" + STATION_ID_PATH_VAR,
            UPDATE_STATION_PATH = GET_STATION_PATH,
            DELETE_STATION_PATH = GET_STATION_PATH;

    private final StationRequester stationRequester;
    private static final Logger log = LoggerFactory.getLogger(StationController.class);

    public StationController(StationRequester stationRequester) {
        this.stationRequester = stationRequester;
    }

    @PostMapping(CREATE_STATION_PATH)
    public ResponseEntity createStation (@RequestBody StationInput stationInput) throws Exception {
        if(stationInput.longitude < -180 || stationInput.longitude >  180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if(stationInput.latitude < -90 || stationInput.latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if(!isStationTypeCorrect(stationInput.stationType)) {
            log.info("Container type received was invalid: {}", stationInput.stationType);
            throw new IllegalArgumentException("Container type is invalid. Verify if the type is one of these: " + StationMapper.STATION_TYPES.toString());
        }

        int stationId = stationRequester.createStation(stationInput.stationName, stationInput.latitude,
                stationInput.longitude, stationInput.stationType);
        return ResponseEntity.created(new URI(GET_STATION_PATH.replace(STATION_ID_PATH_VAR, "" + stationId))).build();
    }

    @PutMapping(UPDATE_STATION_PATH)
    public ResponseEntity updateStation (@PathVariable(STATION_ID_PATH_VAR_NAME) int stationId,
                                         @RequestBody StationInput stationInput) throws Exception {
        if(stationInput.longitude < -180 || stationInput.longitude >  180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if(stationInput.latitude < -90 || stationInput.latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if(!isStationTypeCorrect(stationInput.stationType)) {
            log.info("Container type received was invalid: {}", stationInput.stationType);
            throw new IllegalArgumentException("Container type is invalid. Verify if the type is one of these: " + StationMapper.STATION_TYPES.toString());
        }
        stationRequester.updateStation(stationId, stationInput.stationName, stationInput.latitude,
                stationInput.longitude, stationInput.stationType);
        return new ResponseEntity(OK);
    }

    @DeleteMapping(DELETE_STATION_PATH)
    public ResponseEntity deleteStation (@PathVariable(STATION_ID_PATH_VAR_NAME) int stationId) throws Exception {
        stationRequester.deleteStation(stationId);
        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value=GET_ALL_STATIONS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllStations (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage)
            throws Exception {
        PaginatedList<Station> stations = stationRequester.getAllStations(pageNumber,rowsPerPage);

        String selfURIWithParams = GET_ALL_STATIONS_PATH + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(stations.totalEntries, GET_ALL_STATIONS_PATH, pageNumber, rowsPerPage),
                extractStationItems(stations.elements),
                new ArrayList<>(),
                of(getStationTemplate())));
    }

    @GetMapping(value=GET_STATION_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity<SirenOutput> getStation (@PathVariable(STATION_ID_PATH_VAR_NAME) int stationId)
            throws Exception {
        Station station = stationRequester.getStationInfo(stationId);

        if(station == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity<>(
                new SirenOutput(of(station), STATION_CLASS)
                        .addAction(getUpdateStationAction(stationId))
                        .addAction(getDeleteStationAction(stationId))
                        .addLink(getStationSelfLink(stationId))
                        .addLink(getStationsListUpLink()),
                OK
        );
    }

    /**
     * Utility methods
     */

    private static List<Item> extractStationItems(List<Station> stations) throws URISyntaxException {
        List<Item> items = new ArrayList<>(stations.size());
        for (Station s :stations) {
            Item item = new Item(new URI(GET_STATION_PATH.replace(STATION_ID_PATH_VAR,"" + s.stationId)))
                    .addProperty(new Property(STATION_ID_FIELD_NAME, of("" + s.stationId), of(STATION_ID_TITLE), empty()))
                    .addProperty(new Property(STATION_NAME_FIELD_NAME, of(s.stationName), of(STATION_NAME_TITLE), empty()))
                    .addProperty(new Property(LATITUDE_FIELD_NAME, of("" + s.latitude), of(LATITUDE_TITLE), empty()))
                    .addProperty(new Property(LONGITUDE_FIELD_NAME, of("" + s.longitude), of(LONGITUDE_TITLE), empty()))
                    .addProperty(new Property(STATION_TYPE_FIELD_NAME, of (s.stationType),of(STATION_TYPE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    private static boolean isStationTypeCorrect(String stationType){
        return StationMapper.STATION_TYPES.stream()
                .filter(field -> field.value.equalsIgnoreCase(stationType))
                .findFirst()
                .isPresent();
    }

    /**
     * Templates
     */

    private static Template getStationTemplate() {
        return new Template()
                .addProperty(new Property(STATION_NAME_FIELD_NAME, empty(), of(STATION_NAME_TITLE), empty()))
                .addProperty(new Property(LATITUDE_FIELD_NAME, empty(), of(LATITUDE_TITLE), empty()))
                .addProperty(new Property(LONGITUDE_FIELD_NAME, empty(), of(LONGITUDE_TITLE), empty()))
                .addProperty(new Property(STATION_TYPE_FIELD_NAME, empty(), of(STATION_TYPE_TITLE), of(StationMapper.STATION_TYPES)));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            UPDATE_STATION_ACTION_NAME = "update-station",
            DELETE_STATION_ACTION_NAME = "delete-station";

    private static SirenAction getUpdateStationAction(int stationId) throws URISyntaxException {
        return new SirenAction(UPDATE_STATION_ACTION_NAME, empty(), PUT,
                new URI(UPDATE_STATION_PATH.replace(STATION_ID_PATH_VAR, "" + stationId)),
                APPLICATION_JSON
        )
                .addField(new Field(STATION_NAME_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(STATION_NAME_TITLE), empty()))
                .addField(new Field(LATITUDE_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(LATITUDE_TITLE), empty()))
                .addField(new Field(LONGITUDE_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(LONGITUDE_TITLE), empty()))
                .addField(new Field(STATION_TYPE_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(STATION_TYPE_TITLE), of(StationMapper.STATION_TYPES)));
    }

    private static SirenAction getDeleteStationAction (int stationId) throws URISyntaxException {
        return new SirenAction(DELETE_STATION_ACTION_NAME, empty(), DELETE,
                new URI(DELETE_STATION_PATH.replace(STATION_ID_PATH_VAR, "" + stationId)),
                ALL
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getStationSelfLink(int stationId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_STATION_PATH.replace(STATION_ID_PATH_VAR, "" + stationId)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getStationsListUpLink() throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ALL_STATIONS_PATH),
                SirenLink.UP_REL
        );
    }
}
