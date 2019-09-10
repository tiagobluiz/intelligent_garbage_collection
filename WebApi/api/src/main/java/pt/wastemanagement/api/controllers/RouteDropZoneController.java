package pt.wastemanagement.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.functions.RouteDropZoneWithLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.RouteDropZoneRequester;
import pt.wastemanagement.api.views.input.RouteDropZoneInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenLink;
import pt.wastemanagement.api.views.output.siren.SirenOutput;
import pt.wastemanagement.api.views.output.siren.SubEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpStatus.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class RouteDropZoneController  {
    //Fields
    public static final String
            ROUTE_ID_FIELD_NAME = "routeId",
            DROP_ZONE_ID_FIELD_NAME = "dropZoneId",
            LATITUDE_FIELD_NAME = "latitude",
            LONGITUDE_FIELD_NAME = "longitude";

    //Fields Titles
    public static final String
            ROUTE_ID_TITLE = "Route Id",
            DROP_ZONE_ID_TITLE = "Drop Zone Id",
            LATITUDE_TITLE = "Latitude",
            LONGITUDE_TITLE = "Longitude";

    //Path Vars
    public static final String
            ROUTE_ID_PATH_VAR_NAME = RouteController.ROUTE_ID_PATH_VAR_NAME,
            ROUTE_ID_PATH_VAR = RouteController.ROUTE_ID_PATH_VAR,
            DROP_ZONE_ID_PATH_VAR_NAME = "drop_zone_id",
            DROP_ZONE_ID_PATH_VAR = "{" + DROP_ZONE_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            ROUTES_PREFIX = RouteController.GET_ROUTE_PATH,
            DROP_ZONES_PREFIX = "/drop-zones";

    //Paths
    public static final String
            CREATE_ROUTE_DROP_ZONE_PATH = ROUTES_PREFIX + DROP_ZONES_PREFIX,
            DELETE_ROUTE_DROP_ZONE_PATH = CREATE_ROUTE_DROP_ZONE_PATH,
            GET_ROUTE_DROP_ZONE_PATH = CREATE_ROUTE_DROP_ZONE_PATH + "/" + DROP_ZONE_ID_PATH_VAR,
            GET_ROUTE_DROP_ZONES_LIST_PATH = ROUTES_PREFIX + DROP_ZONES_PREFIX;

    private final RouteDropZoneRequester routeDropZoneRequester;
    private static final Logger log = LoggerFactory.getLogger(RouteDropZoneController.class);

    public RouteDropZoneController(RouteDropZoneRequester routeDropZoneRequester) {
        this.routeDropZoneRequester = routeDropZoneRequester;
    }

    @PostMapping(CREATE_ROUTE_DROP_ZONE_PATH)
    public ResponseEntity createRouteDropZone (@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                               @RequestBody RouteDropZoneInput routeDropZoneInput) throws Exception {
        if(routeDropZoneInput.dropZoneId < 0)
            throw new IllegalArgumentException("Invalid drop zone identifier, it must be a positive number");
        routeDropZoneRequester.createRouteDropZone(routeId, routeDropZoneInput.dropZoneId);
        return ResponseEntity.created(new URI(GET_ROUTE_DROP_ZONE_PATH
                .replace(ROUTE_ID_PATH_VAR,"" + routeId)
                .replace(DROP_ZONE_ID_PATH_VAR, "" + routeDropZoneInput.dropZoneId)))
                .build();
    }

    @DeleteMapping(DELETE_ROUTE_DROP_ZONE_PATH)
    public ResponseEntity deleteRouteDropZone (@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                               @PathVariable(DROP_ZONE_ID_PATH_VAR_NAME) int dropZoneId) throws Exception {
        routeDropZoneRequester.deleteRouteDropZone(routeId, dropZoneId);
        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value=GET_ROUTE_DROP_ZONES_LIST_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getRouteDropZonesList (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId
    ) throws Exception {
        PaginatedList<RouteDropZoneWithLocation> routeDropZones =
                routeDropZoneRequester.getRouteDropZonesList(pageNumber, rowsPerPage, routeId);

        String selfURIString = GET_ROUTE_DROP_ZONES_LIST_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(routeDropZones.totalEntries, selfURIString, pageNumber, rowsPerPage),
                extractRouteDropZoneWithLocationItems(routeDropZones.elements),
                new ArrayList<>(),
                of(getRouteDropZoneTemplate())
        ));
    }

    @GetMapping(value=GET_ROUTE_DROP_ZONE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getRouteDropZone (@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                            @PathVariable(DROP_ZONE_ID_PATH_VAR_NAME) int dropZoneId) throws Exception {
        RouteDropZoneWithLocation routeDropZone = routeDropZoneRequester.getRouteDropZone(routeId, dropZoneId);

        if(routeDropZone == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(routeDropZone), ROUTE_DROP_ZONE_CLASS)
                        .addAction(getDeleteRouteDropZoneAction(routeId, dropZoneId))
                        .addSubEntity(getRouteInfoSubEntity(routeId, null))
                        .addLink(getRouteDropZoneSelfLink(routeId, dropZoneId))
                        .addLink(getRouteDropZonesListUpLink(routeId)),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractRouteDropZoneWithLocationItems(List<RouteDropZoneWithLocation> routeDropZones)
            throws URISyntaxException {
        List<Item> items = new ArrayList<>(routeDropZones.size());
        for (RouteDropZoneWithLocation dz : routeDropZones){
            Item item = new Item(
                    new URI(GET_ROUTE_DROP_ZONE_PATH
                            .replace(ROUTE_ID_PATH_VAR, "" + dz.routeId)
                            .replace(DROP_ZONE_ID_PATH_VAR, "" + dz.dropZoneId)
                    ))
                    .addProperty(new Property(ROUTE_ID_FIELD_NAME, of("" + dz.routeId), of(ROUTE_ID_TITLE), empty()))
                    .addProperty(new Property(DROP_ZONE_ID_FIELD_NAME, of("" + dz.dropZoneId), of(DROP_ZONE_ID_TITLE), empty()))
                    .addProperty(new Property(LATITUDE_FIELD_NAME, of("" + dz.latitude), of(LATITUDE_TITLE), empty()))
                    .addProperty(new Property(LONGITUDE_FIELD_NAME, of("" + dz.longitude), of(LONGITUDE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getRouteDropZoneTemplate(){
        return new Template()
                .addProperty(new Property(DROP_ZONE_ID_FIELD_NAME, empty(), of(DROP_ZONE_ID_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            DELETE_ROUTE_DROP_ZONE_ACTION_NAME = "delete-route-drop-zone";

    private static SirenAction getDeleteRouteDropZoneAction (int routeId, int dropZoneId) throws URISyntaxException {
        return new SirenAction(DELETE_ROUTE_DROP_ZONE_ACTION_NAME, empty(), HttpMethod.DELETE,
                new URI(DELETE_ROUTE_DROP_ZONE_PATH
                        .replace(ROUTE_ID_PATH_VAR, "" + routeId)
                        .replace(DROP_ZONE_ID_PATH_VAR, "" + dropZoneId)),
                MediaType.ALL
        );
    }

    /**
     * Sub Entity
     */

    private static SubEntity getRouteInfoSubEntity(int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_REL}, properties, empty(), empty(), ROUTE_CLASS
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getRouteDropZoneSelfLink (int routeId, int dropZoneId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ROUTE_DROP_ZONE_PATH
                        .replace(ROUTE_ID_PATH_VAR, "" + routeId)
                        .replace(DROP_ZONE_ID_PATH_VAR, "" + dropZoneId)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getRouteDropZonesListUpLink(int routeId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ROUTE_DROP_ZONES_LIST_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)),
                SirenLink.UP_REL
        );
    }
}
