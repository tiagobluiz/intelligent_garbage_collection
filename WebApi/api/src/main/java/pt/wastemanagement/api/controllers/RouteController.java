package pt.wastemanagement.api.controllers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.functions.RouteStatistics;
import pt.wastemanagement.api.model.functions.RouteWithStationNameAndLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.RouteRequester;
import pt.wastemanagement.api.views.input.RouteInput;
import pt.wastemanagement.api.views.output.GetRoute;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpStatus.*;
import static pt.wastemanagement.api.controllers.CollectZoneController.*;
import static pt.wastemanagement.api.controllers.ContainerController.*;
import static pt.wastemanagement.api.controllers.RouteCollectionController.*;
import static pt.wastemanagement.api.controllers.RouteDropZoneController.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class RouteController  {
    // Fields
    public static final String
            ROUTE_ID_FIELD_NAME = "routeId",
            START_POINT_FIELD_NAME = "startPoint",
            FINISH_POINT_FIELD_NAME = "finishPoint",
            ACTIVE_FIELD_NAME = "active",
            START_POINT_STATION_NAME_FIELD_NAME = "startPointStationName",
            START_POINT_LATITUDE_FIELD_NAME = "startPointLatitude",
            START_POINT_LONGITUDE_FIELD_NAME = "startPointLongitude",
            FINISH_POINT_STATION_NAME_FIELD_NAME = "finishPointStationName",
            FINISH_POINT_LATITUDE_FIELD_NAME = "finishPointLatitude",
            FINISH_POINT_LONGITUDE_FIELD_NAME = "finishPointLongitude";

    // Fields Titles
    public static final String
            ROUTE_ID_TITLE = "Route Id",
            START_POINT_TITLE = "Start Point",
            FINISH_POINT_TITLE = "Finish Point",
            ACTIVE_TITLE = "Active",
            START_POINT_STATION_NAME_TITLE = "Start Point Station Name",
            START_POINT_LATITUDE_TITLE = "Start Point Latitude",
            START_POINT_LONGITUDE_TITLE = "Start Point Longitude",
            FINISH_POINT_STATION_NAME_TITLE = "Finish Point StationName",
            FINISH_POINT_LATITUDE_TITLE = "Finish Point Latitude",
            FINISH_POINT_LONGITUDE_TITLE = "Finish Point Longitude";

    //Path Vars
    public static final String
            ROUTE_ID_PATH_VAR_NAME = "route_id",
            ROUTE_ID_PATH_VAR = "{" + ROUTE_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            ROUTES_PREFIX = "/routes";

    public static final String
    // /routes
            GET_ALL_ROUTES_PATH = ROUTES_PREFIX,
            CREATE_ROUTE_PATH = GET_ALL_ROUTES_PATH,
    // /routes/{route_id}
            GET_ROUTE_PATH = CREATE_ROUTE_PATH + "/" + ROUTE_ID_PATH_VAR,
            UPDATE_ROUTE_PATH = GET_ROUTE_PATH,
    // /routes/{route_id}/...
            ACTIVATE_ROUTE_PATH = GET_ROUTE_PATH + ACTIVATE_PREFIX,
            DEACTIVATE_ROUTE_PATH = GET_ROUTE_PATH + DEACTIVATE_PREFIX;



    private final RouteRequester routeRequester;
    private static final Logger log = LoggerFactory.getLogger(RouteController.class);

    public RouteController(RouteRequester routeRequester) {
        this.routeRequester = routeRequester;
    }

    @PostMapping(CREATE_ROUTE_PATH)
    public ResponseEntity createRoute(@RequestBody RouteInput routeInput) throws Exception {
        if(routeInput.startPoint < 0)
            throw new IllegalArgumentException("Invalid start station identifier, it must be a positive number");
        if(routeInput.finishPoint < 0)
            throw new IllegalArgumentException("Invalid finish station identifier, it must be a positive number");

        int routeId = routeRequester.createRoute(routeInput.startPoint, routeInput.finishPoint);
        List<String> headers = new ArrayList<>();
        headers.add(GET_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_ROUTE_PATH)
    public ResponseEntity updateRoute(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                      @RequestBody RouteInput routeInput) throws Exception {
        if(routeInput.startPoint < 0)
            throw new IllegalArgumentException("Invalid start station identifier, it must be a positive number");
        if(routeInput.finishPoint < 0)
            throw new IllegalArgumentException("Invalid finish station identifier, it must be a positive number");

        routeRequester.updateRoute(routeId, routeInput.startPoint, routeInput.finishPoint);
        return new ResponseEntity(OK);
    }

    @PutMapping(DEACTIVATE_ROUTE_PATH)
    public ResponseEntity deactivateRoute(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        routeRequester.deactivateRoute(routeId);
        return new ResponseEntity(OK);
    }

    @PutMapping(ACTIVATE_ROUTE_PATH)
    public ResponseEntity activateRoute(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        routeRequester.activateRoute(routeId);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_ALL_ROUTES_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllRoutes(
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(value = SHOW_INACTIVE_QUERY_PARAM,  defaultValue = "false") boolean showInactive) throws Exception {
        PaginatedList<RouteWithStationNameAndLocation> routes = routeRequester.getAllRoutes(pageNumber, rowsPerPage, showInactive);

        List<CollectionLink> links = getPageLinks(routes.totalEntries, GET_ALL_ROUTES_PATH, pageNumber, rowsPerPage, showInactive);
        links.add(getCollectableRoutesLink());

        String selfURIString = GET_ALL_ROUTES_PATH + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIString),
                links,
                extractRouteWithStationNameAndLocationItems(routes.elements),
                getShowInactiveQueries(GET_ALL_ROUTES_PATH, rowsPerPage, showInactive),
                of(getRouteTemplate())
        ));
    }

    @GetMapping(value=GET_ROUTE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity<SirenOutput> getRoute(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        RouteWithStationNameAndLocation routeInfo = routeRequester.getRouteInfo(routeId);
        RouteStatistics routeStatistics = routeRequester.getRouteStatistics(routeId);

        if(routeInfo == null && routeInfo == null) return new ResponseEntity<>(NOT_FOUND);

        GetRoute route = new GetRoute(routeInfo,routeStatistics);

        return new ResponseEntity<>(new SirenOutput(of(route),ROUTE_CLASS)
                        .addAction(getUpdateRouteAction(routeId))
                        .addAction(getCreateRouteCollectAction(routeId))
                        .addAction(getDeactivateRouteAction(routeId))
                        .addAction(getActivateRouteAction(routeId))
                        .addSubEntity(getRouteContainersSubEntity(routeId, null))
                        .addSubEntity(getRouteCollectZonesSubEntity(routeId, null))
                        .addSubEntity(getRouteCollectionsSubEntity(routeId, null))
                        .addSubEntity(getRouteCollectionPlanSubEntity(routeId, null))
                        .addSubEntity(getRouteDropZonesSubEntity(routeId, null))
                        .addLink(getRouteSelfLink(routeId))
                        .addLink(getRoutesListUpLink())
                        .addLink(getStationsLink()),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractRouteWithStationNameAndLocationItems(List<RouteWithStationNameAndLocation> routes)
            throws URISyntaxException {
        List<Item> items = new ArrayList<>(routes.size());
        for (RouteWithStationNameAndLocation route : routes) {
            Item item = new Item(new URI(GET_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR, "" + route.routeId)))
                    .addProperty(new Property(ROUTE_ID_FIELD_NAME, of("" + route.routeId), of(ROUTE_ID_TITLE), empty()))
                    .addProperty(new Property(ACTIVE_FIELD_NAME, of(route.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)),
                            of(ACTIVE_TITLE), empty()))
                    .addProperty(new Property(START_POINT_STATION_NAME_FIELD_NAME, of(route.startPointStationName),
                            of(START_POINT_STATION_NAME_TITLE), empty()))
                    .addProperty(new Property(START_POINT_LATITUDE_FIELD_NAME, of("" + route.startPointLatitude),
                            of(START_POINT_LATITUDE_TITLE), empty()))
                    .addProperty(new Property(START_POINT_LONGITUDE_FIELD_NAME, of("" + route.startPointLongitude),
                            of(START_POINT_LONGITUDE_TITLE), empty()))
                    .addProperty(new Property(FINISH_POINT_STATION_NAME_FIELD_NAME, of(route.finishPointStationName),
                            of(FINISH_POINT_STATION_NAME_TITLE), empty()))
                    .addProperty(new Property(FINISH_POINT_LATITUDE_FIELD_NAME, of("" + route.finishPointLatitude),
                            of(FINISH_POINT_LATITUDE_TITLE), empty()))
                    .addProperty(new Property(FINISH_POINT_LONGITUDE_FIELD_NAME, of("" + route.finishPointLongitude),
                            of(FINISH_POINT_LONGITUDE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Collection Links
     */

    private static CollectionLink getCollectableRoutesLink() throws URISyntaxException {
        return new CollectionLink(
                Controller.COLLECTABLE_ROUTES_REL,
                new URI(RouteCollectionController.GET_COLLECTABLE_ROUTES_PATH),
                empty(), of("Routes that are available to be collected"), empty()
        );
    }

    /**
     * Templates
     */

    private static Template getRouteTemplate() {
        return new Template()
                .addProperty(new Property(START_POINT_FIELD_NAME, empty(), of(START_POINT_TITLE), empty()))
                .addProperty(new Property(FINISH_POINT_FIELD_NAME, empty(), of(FINISH_POINT_TITLE), empty()));
    }


    /**
     * SIREN ACTIONS
     */

    // Action names
    public static final String
            UPDATE_ROUTE_ACTION_NAME = "update-route",
            ACTIVATE_ROUTE_ACTION_NAME = "activate-route",
            DEACTIVATE_ROUTE_ACTION_NAME = "deactivate-route",
            CREATE_ROUTE_COLLECT_ACTION_NAME ="create-route-collect";



    private SirenAction getUpdateRouteAction(int routeId) throws URISyntaxException {
        return new SirenAction(UPDATE_ROUTE_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(UPDATE_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)), MediaType.APPLICATION_JSON
        )
                .addField(new Field(START_POINT_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(START_POINT_TITLE), empty()))
                .addField(new Field(FINISH_POINT_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(FINISH_POINT_TITLE), empty()));
    }

    private SirenAction getActivateRouteAction(int routeId) throws URISyntaxException {
        return new SirenAction(ACTIVATE_ROUTE_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(ACTIVATE_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId)), MediaType.ALL);
    }

    private SirenAction getDeactivateRouteAction(int routeId) throws URISyntaxException {
        return new SirenAction(DEACTIVATE_ROUTE_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(DEACTIVATE_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId)), MediaType.ALL);
    }

    private SirenAction getCreateRouteCollectAction(int routeId) throws URISyntaxException {
        return new SirenAction(CREATE_ROUTE_COLLECT_ACTION_NAME, empty(), HttpMethod.POST,
                new URI(RouteCollectionController.CREATE_ROUTE_COLLECTION_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)),
                MediaType.APPLICATION_JSON
        )
                .addField(new Field(RouteCollectionController.START_DATE_FIELD_NAME, empty(), of(DATE_TIME_LOCAL_TYPE), empty(),
                        of(RouteCollectionController.START_DATE_TITLE), empty()))
                .addField(new Field(RouteCollectionController.TRUCK_PLATE_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(),
                        of(RouteCollectionController.TRUCK_PLATE_TITLE), empty()));
    }

    /**
     * Sub Entities
     */

    private static SubEntity getRouteContainersSubEntity (int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_ROUTE_CONTAINERS_PATH
                        .replace(ContainerController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_CONTAINERS_REL}, properties, empty(), empty(), ROUTE_CONTAINERS_CLASS, COLLECTION_CLASS
        );
    }

    private static SubEntity getRouteCollectZonesSubEntity (int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_ROUTE_COLLECT_ZONES_PATH
                        .replace(CollectZoneController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_COLLECT_ZONES_REL}, properties, empty(), empty(), ROUTE_COLLECT_ZONES_CLASS, COLLECTION_CLASS
        );
    }

    private static SubEntity getRouteCollectionsSubEntity (int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(RouteCollectionController.GET_ROUTE_COLLECTIONS_LIST_PATH
                        .replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_COLLECTIONS_REL}, properties, empty(), empty(), ROUTE_COLLECTIONS_CLASS, COLLECTION_CLASS
        );
    }

    private static SubEntity getRouteCollectionPlanSubEntity (int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_ROUTE_COLLECTION_PLAN_PATH
                        .replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_COLLECTION_PLAN_REL}, properties, empty(), empty(), ROUTE_COLLECTION_PLAN_CLASS, COLLECTION_CLASS
        );
    }

    private static SubEntity getRouteDropZonesSubEntity(int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_ROUTE_DROP_ZONES_LIST_PATH
                        .replace(RouteDropZoneController.ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_DROP_ZONES_REL}, properties, empty(), empty(), ROUTE_DROP_ZONE_CLASS, COLLECTION_CLASS
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getRouteSelfLink(int routeId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ROUTE_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getRoutesListUpLink() throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ALL_ROUTES_PATH),
                SirenLink.UP_REL
        );
    }

    private static SirenLink getStationsLink() throws URISyntaxException {
        return new SirenLink(
                new URI(StationController.GET_ALL_STATIONS_PATH),
                STATION_LIST_REL
        );
    }
}
