package pt.wastemanagement.api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.Collect;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectRequester;
import pt.wastemanagement.api.views.input.CollectInput;
import pt.wastemanagement.api.views.output.GetCollect;
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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenLink.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class CollectController {
    //Fields
    public static final String
            CONTAINER_ID_FIELD_NAME = "containerId",
            COLLECT_DATE_FIELD_NAME = "collectDate",
            CONFIRMED_FIELD_NAME = "confirmed";

    //Fields Titles
    public static final String
            CONTAINER_ID_TITLE = "Container Id",
            COLLECT_DATE_TITLE = "Collect Date",
            CONFIRMED_TITLE = "Confirmed";

    //Path Vars
    public static final String
            CONTAINER_ID_PATH_VAR_NAME = ContainerController.CONTAINER_ID_PATH_VAR_NAME,
            CONTAINER_ID_PATH_VAR = ContainerController.CONTAINER_ID_PATH_VAR,
            COLLECT_ZONE_ID_PATH_VAR_NAME = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR_NAME,
            COLLECT_ZONE_ID_PATH_VAR = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR,
            COLLECT_DATE_PATH_VAR_NAME = "collect_date",
            COLLECT_DATE_PATH_VAR = "{" + COLLECT_DATE_PATH_VAR_NAME + "}";

    //Path Partitions
    private static final String
            CONTAINERS_PREFIX = ContainerController.GET_CONTAINER_PATH,
            COLLECT_ZONES_PREFIX = CollectZoneController.GET_COLLECT_ZONE_PATH,
            COLLECTS_PREFIX = "/collects";

    //Paths
    public static final String
    // /containers/{container_id}/collects
            CREATE_COLLECT_PATH = CONTAINERS_PREFIX + COLLECTS_PREFIX,
            GET_CONTAINER_COLLECTS_PATH = CREATE_COLLECT_PATH,
    // /containers/{container_id}/collects/{collect_date}
            GET_COLLECT_PATH = CREATE_COLLECT_PATH + "/" + COLLECT_DATE_PATH_VAR,
            UPDATE_COLLECT_PATH = GET_COLLECT_PATH,
    // /collect-zones/{collect_zone_id}/collects
            COLLECT_COLLECT_ZONE_CONTAINERS_PATH = COLLECT_ZONES_PREFIX + COLLECTS_PREFIX;

    private final CollectRequester collectRequester;

    public CollectController(CollectRequester collectRequester) {
        this.collectRequester = collectRequester;
    }

    @PostMapping(CREATE_COLLECT_PATH)
    public ResponseEntity createCollect (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                         @RequestBody CollectInput collectInput) throws Exception {
        collectRequester.createCollect(containerId, collectInput.collectDate);
        List<String> headers = new ArrayList<>();
        headers.add(GET_COLLECT_PATH
                .replace(CONTAINER_ID_PATH_VAR,"" + containerId)
                .replace(COLLECT_DATE_PATH_VAR, collectInput.collectDate.toString()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PostMapping(COLLECT_COLLECT_ZONE_CONTAINERS_PATH)
    public ResponseEntity collectCollectZoneContainers(@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId,
                                                       @RequestBody CollectInput collectInput) throws Exception {
        collectRequester.collectCollectZoneContainers(collectZoneId, collectInput.collectDate, collectInput.containerType);
        return new ResponseEntity(CREATED);
    }

    @PutMapping(UPDATE_COLLECT_PATH)
    public ResponseEntity updateCollect (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                         @PathVariable(COLLECT_DATE_PATH_VAR_NAME) LocalDateTime collectDate,
                                         @RequestBody CollectInput collectInput) throws Exception {
        collectRequester.updateCollect(containerId,collectDate, collectInput.collectDate);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_CONTAINER_COLLECTS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getContainerCollects (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId) throws Exception {
        PaginatedList<Collect> collects = collectRequester.getContainerCollects(pageNumber, rowsPerPage, containerId);

        String selfURIString = GET_CONTAINER_COLLECTS_PATH.replace(CONTAINER_ID_PATH_VAR, "" + containerId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                Controller.getPageLinks(collects.totalEntries, selfURIString, pageNumber, rowsPerPage),
                extractCollectItems(collects.elements),
                new ArrayList<>(),
                of(getCollectTemplate())));
    }

    @GetMapping(value=GET_COLLECT_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getCollect (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                      @PathVariable(COLLECT_DATE_PATH_VAR_NAME) LocalDateTime collectDate)
            throws Exception {
        Collect collect = collectRequester.getContainerCollect(containerId, collectDate);

        if(collect == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(new SirenOutput(of(new GetCollect(collect)), COLLECT_CLASS)
                .addAction(getUpdateCollectAction(containerId, collectDate))
                .addLink(getContainerUpLink(containerId))
                .addLink(getCollectSelfLink(containerId, collectDate)),
                OK);
    }

    /**
     * Utility Methods
     */

    private static List<Item> extractCollectItems(List<Collect> collects) throws URISyntaxException {
        List<Item> items = new ArrayList<>(collects.size());
        for (Collect c :collects) {
            Item item = new Item(
                    new URI(GET_COLLECT_PATH
                            .replace(CONTAINER_ID_PATH_VAR, "" + c.containerId)
                            .replace(COLLECT_DATE_PATH_VAR, c.collectDate.toString())
                    ))
                    .addProperty(new Property(CONTAINER_ID_FIELD_NAME, of("" + c.containerId), of(CONTAINER_ID_TITLE), empty()))
                    .addProperty(new Property(COLLECT_DATE_FIELD_NAME, of(c.collectDate.toString()),of(COLLECT_DATE_TITLE), empty()))
                    .addProperty(new Property(CONFIRMED_FIELD_NAME, of(c.confirmed.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)),
                            of(CONFIRMED_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getCollectTemplate() {
        return new Template()
                .addProperty(new Property(COLLECT_DATE_FIELD_NAME, empty(),of(COLLECT_DATE_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            UPDATE_COLLECT_ACTION_NAME = "update-collect";


    private static SirenAction getUpdateCollectAction(int containerId, LocalDateTime collectDate) throws URISyntaxException {
        return new SirenAction(UPDATE_COLLECT_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(UPDATE_COLLECT_PATH
                        .replace(CONTAINER_ID_PATH_VAR, "" + containerId)
                        .replace(COLLECT_DATE_PATH_VAR, collectDate.toString())),
                MediaType.APPLICATION_JSON
        ).addField(new Field(COLLECT_DATE_FIELD_NAME, empty(), of(DATE_TIME_LOCAL_TYPE), empty(), of(COLLECT_DATE_TITLE), empty()));
    }

    /**
     * Siren Links
     */

    private static SirenLink getContainerUpLink(int containerId) throws URISyntaxException {
        return new SirenLink(new URI(ContainerController.GET_CONTAINER_PATH.replace(ContainerController.CONTAINER_ID_PATH_VAR, "" + containerId)), UP_REL);
    }

    private static SirenLink getCollectSelfLink(int containerId, LocalDateTime collectDate) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_COLLECT_PATH
                        .replace(CONTAINER_ID_PATH_VAR, "" + containerId)
                        .replace(COLLECT_DATE_PATH_VAR, collectDate.toString())
                ), SELF_REL
        );
    }
}
