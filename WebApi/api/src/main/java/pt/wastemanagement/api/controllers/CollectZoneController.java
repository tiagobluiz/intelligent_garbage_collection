package pt.wastemanagement.api.controllers;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.model.functions.CollectZoneStatistics;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocation;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocationAndOccupationInfo;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectZoneRequester;
import pt.wastemanagement.api.views.input.CollectZoneInput;
import pt.wastemanagement.api.views.output.GetCollectZone;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenLink.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class CollectZoneController {
    //Fields
    public static final String
            COLLECT_ZONE_ID_FIELD_NAME = "collectZoneId",
            ROUTE_ID_FIELD_NAME = "routeId",
            PICK_ORDER_FIELD_NAME = "pickOrder",
            ACTIVE_FIELD_NAME = "active",
            LATITUDE_FIELD_NAME = "latitude",
            LONGITUDE_FIELD_NAME = "longitude";

    //Fields Titles
    public static final String
            COLLECT_ZONE_ID_TITLE = "Collect Zone Id",
            ROUTE_ID_TITLE = "Route Id",
            PICK_ORDER_TITLE= "Pick Order",
            ACTIVE_TITLE= "Active",
            LATITUDE_TITLE = "Latitude",
            LONGITUDE_TITLE = "Longitude";

    //Path Vars
    public static final String
            ROUTE_ID_PATH_VAR_NAME = RouteController.ROUTE_ID_PATH_VAR_NAME,
            ROUTE_ID_PATH_VAR = RouteController.ROUTE_ID_PATH_VAR,
            COLLECT_ZONE_ID_PATH_VAR_NAME = "collect_zone_id",
            COLLECT_ZONE_ID_PATH_VAR = "{" + COLLECT_ZONE_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            COLLECT_ZONES_PREFIX = "/collect-zones",
            ROUTES_PREFIX = "/routes";

    //Query Parameters
    public static final String
            LATITUDE_QUERY_PARAM = "latitude",
            LONGITUDE_QUERY_PARAM = "longitude",
            RANGE_QUERY_PARAM = "range";

    //Paths
    public static final String
    // /routes/{route_id}/collect-zones
            GET_ROUTE_COLLECT_ZONES_PATH = ROUTES_PREFIX + "/" + ROUTE_ID_PATH_VAR + COLLECT_ZONES_PREFIX,
            CREATE_COLLECT_ZONE_PATH = GET_ROUTE_COLLECT_ZONES_PATH,
    // /collect-zones/
            GET_COLLECT_ZONES_IN_RANGE_PATH = COLLECT_ZONES_PREFIX,
            GET_COLLECT_ZONES_IN_RANGE_PATH_WITH_QUERY_PARAMS = GET_COLLECT_ZONES_IN_RANGE_PATH + "?" + LATITUDE_QUERY_PARAM +
                    "={" + LATITUDE_QUERY_PARAM + "}&" + LONGITUDE_QUERY_PARAM + "={" + LONGITUDE_QUERY_PARAM + "}&" + RANGE_QUERY_PARAM + "={" +
                    RANGE_QUERY_PARAM + "}",
    // /collect-zones/{collect_zone_id}
            GET_COLLECT_ZONE_PATH = COLLECT_ZONES_PREFIX + "/" + COLLECT_ZONE_ID_PATH_VAR,
            UPDATE_COLLECT_ZONE_PATH = GET_COLLECT_ZONE_PATH,
            ACTIVATE_COLLECT_ZONE_PATH = GET_COLLECT_ZONE_PATH + ACTIVATE_PREFIX,
            DEACTIVATE_COLLECT_ZONE_PATH = GET_COLLECT_ZONE_PATH + DEACTIVATE_PREFIX;

    private final CollectZoneRequester collectZoneRequester;

    public CollectZoneController(CollectZoneRequester collectZoneRequester) {
        this.collectZoneRequester = collectZoneRequester;
    }

    @PostMapping(CREATE_COLLECT_ZONE_PATH)
    public ResponseEntity createCollectZone(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        int collectZoneId =  collectZoneRequester.createCollectZone(routeId);
        List<String> headers = new ArrayList<>();
        headers.add(GET_COLLECT_ZONE_PATH
                .replace(ROUTE_ID_PATH_VAR,"" + routeId)
                .replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)
        );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_COLLECT_ZONE_PATH)
    public ResponseEntity updateCollectZone(@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId,
                                            @RequestBody CollectZoneInput collectZoneInput) throws Exception {
        if(collectZoneInput.routeId < 0)
            throw new IllegalArgumentException("Invalid route id. It MUST be a positive number");

        collectZoneRequester.updateCollectZone(collectZoneId,collectZoneInput.routeId);
        return new ResponseEntity(OK);
    }

    @PutMapping(ACTIVATE_COLLECT_ZONE_PATH)
    public ResponseEntity activateCollectZone(@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId) throws Exception {
        collectZoneRequester.activateCollectZone(collectZoneId);
        return new ResponseEntity(OK);
    }

    @PutMapping(DEACTIVATE_COLLECT_ZONE_PATH)
    public ResponseEntity deactivateCollectZone(@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId) throws Exception {
        collectZoneRequester.deactivateCollectZone(collectZoneId);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_ROUTE_COLLECT_ZONES_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getRouteCollectZones(
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(value = SHOW_INACTIVE_QUERY_PARAM,  defaultValue = "false") boolean showInactive,
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId)
            throws Exception {
        PaginatedList<CollectZoneWithLocation> routeCollectZones =
                collectZoneRequester.getRouteCollectZones(pageNumber,rowsPerPage,routeId,showInactive);

        String selfURIString = GET_ROUTE_COLLECT_ZONES_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(routeCollectZones.totalEntries,selfURIString,pageNumber,rowsPerPage,showInactive),
                extractCollectZoneWithLocationItems(routeCollectZones.elements, ""),
                getShowInactiveQueries(selfURIString, rowsPerPage, showInactive),
                of(getCollectZoneTemplate(routeId)))
        );
    }



    @GetMapping(value=GET_COLLECT_ZONE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getCollectZone (@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId)
            throws Exception {
        CollectZoneWithLocationAndOccupationInfo collectZoneInfo = collectZoneRequester.getCollectZoneInfo(collectZoneId);
        CollectZoneStatistics collectZoneStatistics = collectZoneRequester.getCollectZoneStatistics(collectZoneId);

        if(collectZoneInfo == null && collectZoneStatistics == null) return new ResponseEntity(NOT_FOUND);

        GetCollectZone getCollectZone = new GetCollectZone(collectZoneInfo,collectZoneStatistics);

        return new ResponseEntity(new SirenOutput(of(getCollectZone), COLLECT_ZONE_CLASS)
                        .addAction(getUpdateCollectZoneAction(collectZoneId))
                        .addAction(getDeactivateCollectZoneAction(collectZoneId))
                        .addAction(getActivateCollectZoneAction(collectZoneId))
                        .addSubEntity(getCollectZoneContainersSubEntity(collectZoneId, null))
                        .addLink(getCollectZoneSelfLink(collectZoneId))
                        .addLink(getRoutesLink())
                        .addLink(getCollectZoneRouteUpLink(collectZoneInfo.routeId)),
                HttpStatus.OK);
    }

    @GetMapping(value = GET_COLLECT_ZONES_IN_RANGE_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getCollectZonesInRange(@RequestParam(LATITUDE_QUERY_PARAM) float latitude,
                                                     @RequestParam(LONGITUDE_QUERY_PARAM) float longitude,
                                                     @RequestParam(value = RANGE_QUERY_PARAM, defaultValue = "25") int range
    ) throws Exception {
        if(range <= 0)
            throw new IllegalArgumentException("Range must be a positive number, higher than 0");

        List<CollectZoneWithLocation> collectZones = collectZoneRequester.getCollectZonesInRange(latitude, longitude, range);

        String selfURIWithParams = GET_COLLECT_ZONES_IN_RANGE_PATH + "?" + LATITUDE_QUERY_PARAM + "=" + latitude + "&" + LONGITUDE_QUERY_PARAM + "=" +
                longitude + "&" + RANGE_QUERY_PARAM + "=" + range;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                new ArrayList<>(),
                extractCollectZoneWithLocationItems(collectZones, ""),
                new ArrayList<>(),
                empty())
        );

    }


    /**
     * Utility methods
     */

    public static List<Item> extractCollectZoneWithLocationItems(List<CollectZoneWithLocation> routeCollectZones, String containerType)
            throws URISyntaxException {
        List<Item> items = new ArrayList<>(routeCollectZones.size());
        for(CollectZoneWithLocation cz: routeCollectZones){
            Item item = new Item(
                    new URI(GET_COLLECT_ZONE_PATH
                            .replace(ROUTE_ID_PATH_VAR, "" + cz.routeId)
                            .replace(COLLECT_ZONE_ID_PATH_VAR, "" + cz.collectZoneId))
            )
                    .addProperty(new Property(COLLECT_ZONE_ID_FIELD_NAME, of("" + cz.collectZoneId), of(COLLECT_ZONE_ID_TITLE), empty()))
                    .addProperty(new Property(ROUTE_ID_FIELD_NAME, of("" + cz.routeId), of(ROUTE_ID_TITLE), empty()))
                    .addProperty(new Property(PICK_ORDER_FIELD_NAME, of("" + cz.pickOrder), of(PICK_ORDER_TITLE), empty()))
                    .addProperty(new Property(ACTIVE_FIELD_NAME, of(cz.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)),
                            of(ACTIVE_TITLE), empty()))
                    .addProperty(new Property(LATITUDE_FIELD_NAME, of("" + cz.latitude),of(LATITUDE_TITLE), empty()))
                    .addProperty(new Property(LONGITUDE_FIELD_NAME, of("" + cz.longitude), of(LONGITUDE_TITLE), empty()));

            if(!containerType.equals("")) {
                item
                        .addLink(new CollectionLink(COLLECT_COLLECT_ZONE_CONTAINERS_REL,
                        new URI(CollectController.COLLECT_COLLECT_ZONE_CONTAINERS_PATH
                                .replace(CollectController.COLLECT_ZONE_ID_PATH_VAR, String.valueOf(cz.collectZoneId))),
                        empty(), empty(), empty()))
                        .addLink(new CollectionLink(WASH_COLLECT_ZONE_CONTAINERS_REL,
                                new URI(WashController.WASH_COLLECT_ZONE_CONTAINERS_PATH
                                        .replace(WashController.COLLECT_ZONE_ID_PATH_VAR, String.valueOf(cz.collectZoneId))),
                                empty(), empty(), empty()));
            }
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getCollectZoneTemplate(int routeId){
        return new Template()
                .addProperty(new Property(ROUTE_ID_FIELD_NAME, of("" + routeId), of(ROUTE_ID_TITLE), empty()));
    }

    /**
     * Siren actions
     */

    //Action names
    public static final String
            UPDATE_COLLECT_ZONE_ACTION_NAME = "update-collect-zone",
            DEACTIVATE_COLLECT_ZONE_ACTION_NAME = "deactivate-collect-zone",
            ACTIVATE_COLLECT_ZONE_ACTION_NAME = "activate-collect-zone";

    private static SirenAction getUpdateCollectZoneAction(int collectZoneId) throws URISyntaxException {
        return new SirenAction(UPDATE_COLLECT_ZONE_ACTION_NAME, empty(), PUT,
                new URI(UPDATE_COLLECT_ZONE_PATH.replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                APPLICATION_JSON
        ).addField(new Field(ROUTE_ID_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(ROUTE_ID_TITLE), empty()));
    }

    private static SirenAction getDeactivateCollectZoneAction(int collectZoneId) throws URISyntaxException {
        return new SirenAction(DEACTIVATE_COLLECT_ZONE_ACTION_NAME, empty(), PUT,
                new URI(DEACTIVATE_COLLECT_ZONE_PATH.replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                ALL
        );
    }

    private static SirenAction getActivateCollectZoneAction(int collectZoneId) throws URISyntaxException {
        return new SirenAction(ACTIVATE_COLLECT_ZONE_ACTION_NAME, empty(), PUT,
                new URI(ACTIVATE_COLLECT_ZONE_PATH.replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                ALL
        );
    }


    /**
     * Siren links
     */

    private static SirenLink getCollectZoneSelfLink(int collectZoneId) throws URISyntaxException {
        return new SirenLink(new URI(GET_COLLECT_ZONE_PATH.replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                SELF_REL);
    }

    private static SirenLink getCollectZoneRouteUpLink(int routeId) throws URISyntaxException {
        return new SirenLink(new URI(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId)),
                UP_REL);
    }

    private static SirenLink getRoutesLink() throws URISyntaxException {
        return new SirenLink(new URI(RouteController.GET_ALL_ROUTES_PATH),
                ROUTE_LIST_REL);
    }

    /**
     * Sub entities
     */

    private static SubEntity getCollectZoneContainersSubEntity(int collectZoneId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(ContainerController.GET_COLLECT_ZONE_CONTAINERS_PATH
                        .replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                new String[]{COLLECT_ZONE_CONTAINERS_REL}, properties, empty(), empty(), COLLECT_ZONE_CONTAINERS_CLASS, COLLECTION_CLASS
        );
    }
}
