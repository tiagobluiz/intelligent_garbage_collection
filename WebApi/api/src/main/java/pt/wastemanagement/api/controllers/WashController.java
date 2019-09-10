package pt.wastemanagement.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.model.Wash;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.WashRequester;
import pt.wastemanagement.api.views.input.WashInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.Field;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenLink;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpStatus.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenLink.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class WashController {
    //Fields
    public static final String
            CONTAINER_ID_FIELD_NAME = "containerId",
            WASH_DATE_FIELD_NAME = "washDate";

    //Fields Titles
    public static final String
            CONTAINER_ID_TITLE = "Container Id",
            WASH_DATE_TITLE = "Wash Date";

    //Path Vars
    public static final String
            CONTAINER_ID_PATH_VAR_NAME = ContainerController.CONTAINER_ID_PATH_VAR_NAME,
            CONTAINER_ID_PATH_VAR = ContainerController.CONTAINER_ID_PATH_VAR,
            COLLECT_ZONE_ID_PATH_VAR_NAME = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR_NAME,
            COLLECT_ZONE_ID_PATH_VAR = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR,
            WASH_DATE_PATH_VAR_NAME = "wash_date",
            WASH_DATE_PATH_VAR = "{" + WASH_DATE_PATH_VAR_NAME + "}";

    //Path Partitions
    private static final String
            CONTAINERS_PREFIX = ContainerController.GET_CONTAINER_PATH,
            COLLECT_ZONES_PREFIX = CollectZoneController.GET_COLLECT_ZONE_PATH,
            WASHES_PREFIX = "/washes";

    //Paths
    public static final String
    // /containers/{container_id}/washes
            CREATE_WASH_PATH = CONTAINERS_PREFIX + WASHES_PREFIX,
            GET_CONTAINER_WASHES_PATH = CREATE_WASH_PATH,
    // /containers/{container_id}/washes/{wash_date}
            GET_WASH_PATH = CREATE_WASH_PATH + "/" + WASH_DATE_PATH_VAR,
            UPDATE_WASH_PATH = GET_WASH_PATH,
    // /collect-zones/{collect_zone_id}/washes
            WASH_COLLECT_ZONE_CONTAINERS_PATH = COLLECT_ZONES_PREFIX + WASHES_PREFIX;

    private final WashRequester washRequester;
    private static final Logger log = LoggerFactory.getLogger(WashController.class);

    public WashController(WashRequester washRequester) {
        this.washRequester = washRequester;
    }

    @PostMapping(CREATE_WASH_PATH)
    public ResponseEntity createWash (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                      @RequestBody WashInput washInput) throws Exception {
        washRequester.createWash(containerId, washInput.washDate);
        List<String> headers = new ArrayList<>();
        headers.add(GET_WASH_PATH
                .replace(CONTAINER_ID_PATH_VAR,"" + containerId)
                .replace(WASH_DATE_PATH_VAR, washInput.washDate.toString()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PostMapping(WASH_COLLECT_ZONE_CONTAINERS_PATH)
    public ResponseEntity washCollectZoneContainers(@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId,
                                                    @RequestBody WashInput washInput) throws Exception{
        washRequester.washCollectZoneContainers(collectZoneId, washInput.washDate, washInput.containerType);
        return new ResponseEntity(CREATED);
    }

    @PutMapping(UPDATE_WASH_PATH)
    public ResponseEntity updateWash (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                      @PathVariable(WASH_DATE_PATH_VAR_NAME) LocalDateTime washDate,
                                      @RequestBody WashInput washInput) throws Exception {
        washRequester.updateWash(containerId,washDate, washInput.washDate);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_CONTAINER_WASHES_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getContainerWashes (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId) throws Exception {
        PaginatedList<Wash> washes = washRequester.getContainerWashes(pageNumber, rowsPerPage, containerId);

        String selfURIString = GET_CONTAINER_WASHES_PATH.replace(CONTAINER_ID_PATH_VAR, "" + containerId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(washes.totalEntries, selfURIString, pageNumber, rowsPerPage),
                extractWashItems(washes.elements),
                new ArrayList<>(),
                of(getWashTemplate())
        ));
    }

    @GetMapping(value=GET_WASH_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getWash (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                      @PathVariable(WASH_DATE_PATH_VAR_NAME) LocalDateTime washDate) throws Exception {
        Wash wash = washRequester.getContainerWash(containerId, washDate);

        if(wash == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(wash), WASH_CLASS)
                        .addAction(getUpdateWashAction(containerId, washDate))
                        .addLink(getWashSelfLink(containerId, washDate.toString()))
                        .addLink(getWasheslistUpLink(containerId)),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractWashItems(List<Wash> washes) throws URISyntaxException {
        List<Item> items = new ArrayList<>(washes.size());
        for (Wash w : washes) {
            Item item = new Item(
                    new URI(GET_WASH_PATH
                            .replace(CONTAINER_ID_PATH_VAR, "" + w.containerId)
                            .replace(WASH_DATE_PATH_VAR, w.washDate.toString())
                    ))
                    .addProperty(new Property(CONTAINER_ID_FIELD_NAME, of("" + w.containerId), of(CONTAINER_ID_TITLE), empty()))
                    .addProperty(new Property(WASH_DATE_FIELD_NAME, of(w.washDate.toString()),of(WASH_DATE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getWashTemplate() {
        return new Template()
                .addProperty(new Property(WASH_DATE_FIELD_NAME, empty(),of(WASH_DATE_TITLE), empty()));
    }

    /**
     * Siren Actions
     */
    //Action names
    public static final String
            UPDATE_WASH_ACTION_NAME = "update-wash";


    private static SirenAction getUpdateWashAction(int containerId, LocalDateTime washDate) throws URISyntaxException {
        return new SirenAction(UPDATE_WASH_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(UPDATE_WASH_PATH
                        .replace(CONTAINER_ID_PATH_VAR, "" + containerId)
                        .replace(WASH_DATE_PATH_VAR, washDate.toString())),
                MediaType.APPLICATION_JSON
        ).addField(new Field(WASH_DATE_FIELD_NAME, empty(), of(DATE_TIME_LOCAL_TYPE), empty(), of(WASH_DATE_TITLE), empty()));
    }

    /**
     * Siren Links
     */

    private static SirenLink getWasheslistUpLink(int containerId) throws URISyntaxException {
        return new SirenLink(new URI(GET_CONTAINER_WASHES_PATH.replace(CONTAINER_ID_PATH_VAR, "" + containerId)), UP_REL);
    }

    private static SirenLink getWashSelfLink(int containerId, String collectDate) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_WASH_PATH
                        .replace(CONTAINER_ID_PATH_VAR, "" + containerId)
                        .replace(WASH_DATE_PATH_VAR, collectDate)
                ), SELF_REL
        );
    }
}
