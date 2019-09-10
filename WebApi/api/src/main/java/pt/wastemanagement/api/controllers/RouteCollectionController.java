package pt.wastemanagement.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.mappers.ContainerMapper;
import pt.wastemanagement.api.mappers.RouteMapper;
import pt.wastemanagement.api.model.Route;
import pt.wastemanagement.api.model.RouteCollection;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocation;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectZoneRequester;
import pt.wastemanagement.api.requesters.RouteCollectionRequester;
import pt.wastemanagement.api.requesters.RouteRequester;
import pt.wastemanagement.api.views.input.CollectRouteInput;
import pt.wastemanagement.api.views.input.RouteCollectionInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpStatus.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class RouteCollectionController  {
    //Fields
    public static final String
            ROUTE_ID_FIELD_NAME = "routeId",
            START_DATE_FIELD_NAME = "startDate",
            FINISH_DATE_FIELD_NAME = "finishDate",
            TRUCK_PLATE_FIELD_NAME = "truckPlate",
            CONTAINER_TYPE_FIELD_NAME = "containerType",
            LATITUDE_FIELD_NAME = "latitude",
            LONGITUDE_FIELD_NAME = "longitude";

    //Fields Title
    public static final String
            ROUTE_ID_TITLE = "Route Id",
            START_DATE_TITLE = "Start Date",
            FINISH_DATE_TITLE = "Finish Date",
            TRUCK_PLATE_TITLE = "Truck Plate",
            CONTAINER_TYPE_TITLE = "Container Type",
            LATITUDE_TITLE = "Latitude",
            LONGITUDE_TITLE = "Longitude";

    //Path variables
    public static final String
            ROUTE_ID_PATH_VAR_NAME = RouteController.ROUTE_ID_PATH_VAR_NAME,
            ROUTE_ID_PATH_VAR = RouteController.ROUTE_ID_PATH_VAR,
            TRUCK_PLATE_PATH_VAR_NAME = "truck_plate",
            TRUCK_PLATE_PATH_VAR = "{" + TRUCK_PLATE_PATH_VAR_NAME + "}",
            START_DATE_PATH_VAR_NAME = "start_date",
            START_DATE_PATH_VAR = "{" + START_DATE_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            ROUTES_PREFIX = "/routes",
            TRUCKS_PREFIX = "/trucks",
            COLLECTS_PREFIX = "/collects",
            COLLECTION_PLAN_PREFIX = "/plan";

    //Path query parameters
    public static final String
            CONTAINER_TYPE_QUERY_PARAM = "type";

    public static final String
    // /routes/collects
            COLLECT_ROUTE_PATH = ROUTES_PREFIX + COLLECTS_PREFIX,
            GET_COLLECTABLE_ROUTES_PATH = COLLECT_ROUTE_PATH,
    // /routes/{route_id}/collects
            CREATE_ROUTE_COLLECTION_PATH = ROUTES_PREFIX + "/" + ROUTE_ID_PATH_VAR + COLLECTS_PREFIX,
            GET_ROUTE_COLLECTIONS_LIST_PATH = CREATE_ROUTE_COLLECTION_PATH,
    // /routes/{route_id}/plan
            GET_ROUTE_COLLECTION_PLAN_PATH = ROUTES_PREFIX + "/" + ROUTE_ID_PATH_VAR + COLLECTION_PLAN_PREFIX,
    // /routes/{route_id}/collects/{start_date}
            GET_ROUTE_COLLECTION_PATH = CREATE_ROUTE_COLLECTION_PATH + "/" + START_DATE_PATH_VAR,
            UPDATE_ROUTE_COLLECTION_PATH = GET_ROUTE_COLLECTION_PATH,
    // /trucks/{truck_plate}/collects
            GET_TRUCK_COLLECTIONS_PATH = TRUCKS_PREFIX + "/" + TRUCK_PLATE_PATH_VAR + COLLECTS_PREFIX;


    private final RouteCollectionRequester routeCollectionRequester;
    private final RouteRequester routeRequester;
    private final CollectZoneRequester collectZoneRequester;
    private static final Logger log = LoggerFactory.getLogger(RouteCollectionController.class);

    public RouteCollectionController(RouteCollectionRequester routeCollectionRequester,
                                     RouteRequester routeRequester,
                                     CollectZoneRequester collectZoneRequester){
        this.routeCollectionRequester = routeCollectionRequester;
        this.routeRequester = routeRequester;
        this.collectZoneRequester = collectZoneRequester;
    }

    @PostMapping(COLLECT_ROUTE_PATH)
    public ResponseEntity collectRoute(@RequestBody CollectRouteInput collectRouteInput) throws Exception {
        if(!collectRouteInput.truckPlate.matches("\\w{2}-\\w{2}-\\w{2}")) //This implementation varies from country
            throw new IllegalArgumentException("Truck registration plate has an invalid format");
        if(collectRouteInput.longitude < -180 || collectRouteInput.longitude >  180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if(collectRouteInput.latitude < -90 || collectRouteInput.latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if(!ContainerController.isContainerTypeValid(collectRouteInput.containerType)){
            log.info("Container type received was invalid: {}", collectRouteInput.containerType);
            throw new IllegalArgumentException("Container type is invalid. Verify if the type is one of these: " + ContainerMapper.CONTAINER_TYPES.toString());
        }
        int routeId = routeCollectionRequester
                .collectRoute(collectRouteInput.latitude, collectRouteInput.longitude,
                        collectRouteInput.startDate,collectRouteInput.truckPlate, collectRouteInput.containerType);
        List<String> headers = new ArrayList<>();
        headers.add(GET_ROUTE_COLLECTION_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId)
                .replace(START_DATE_PATH_VAR, collectRouteInput.startDate.toString()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PostMapping(CREATE_ROUTE_COLLECTION_PATH)
    public ResponseEntity createRouteCollection (@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                                 @RequestBody RouteCollectionInput routeCollectionInput) throws Exception {
        if(!routeCollectionInput.truckPlate.matches("\\w{2}-\\w{2}-\\w{2}")) //This implementation varies from country
            throw new IllegalArgumentException("Truck registration plate has an invalid format");

        routeCollectionRequester
                .createRouteCollection(
                        routeId,
                        routeCollectionInput.startDate,
                        routeCollectionInput.truckPlate
                );
        List<String> headers = new ArrayList<>();
        headers.add(GET_ROUTE_COLLECTION_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId)
                .replace(START_DATE_PATH_VAR, routeCollectionInput.startDate.toString()));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_ROUTE_COLLECTION_PATH)
    public ResponseEntity updateRouteCollection (@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                                 @PathVariable(START_DATE_PATH_VAR_NAME) LocalDateTime startDate,
                                                 @RequestBody RouteCollectionInput routeCollectionInput) throws Exception {
        if(!routeCollectionInput.truckPlate.matches("\\w{2}-\\w{2}-\\w{2}")) //This implementation varies from country
            throw new IllegalArgumentException("Truck registration plate has an invalid format");

        routeCollectionRequester.updateRouteCollectionTruck(
                routeId, startDate,
                routeCollectionInput.finishDate,
                routeCollectionInput.truckPlate
        );
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_COLLECTABLE_ROUTES_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getCollectableRoutes (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(CONTAINER_TYPE_QUERY_PARAM) String containerType) throws Exception {
        PaginatedList<Route> routes = routeRequester.getCollectableRoutes(pageNumber, rowsPerPage, containerType);

        String selfURIString = GET_COLLECTABLE_ROUTES_PATH;
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + CONTAINER_TYPE_QUERY_PARAM + "=" + containerType;

        return new CollectionPlusJson(
                new CollectionJson(
                        new URI(selfURIWithParams),
                        getPageLinks(routes.totalEntries, selfURIString, pageNumber, rowsPerPage),
                        extractRouteItems(routes.elements),
                        new ArrayList<>(),
                        routes.totalEntries > 0 ? of(getCollectRouteTemplate()) : empty()));
    }

    @GetMapping(value= GET_ROUTE_COLLECTIONS_LIST_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getRouteCollections(
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        PaginatedList<RouteCollection> routeCollections =
                routeCollectionRequester.getRouteCollections(pageNumber,rowsPerPage,routeId);

        String selfURIString = GET_ROUTE_COLLECTIONS_LIST_PATH.replace(ROUTE_ID_PATH_VAR,"" + routeId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(
                new CollectionJson(new URI(selfURIWithParams),
                        getPageLinks(routeCollections.totalEntries,selfURIString,pageNumber, rowsPerPage),
                        extractRouteCollectionItems(routeCollections.elements),
                        new ArrayList<>(),
                        of(getRouteCollectionTemplate())));
    }

    @GetMapping(value=GET_ROUTE_COLLECTION_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getRouteCollection(@PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
                                             @PathVariable(START_DATE_PATH_VAR_NAME) LocalDateTime startDate)
            throws Exception {
        RouteCollection routeCollection = routeCollectionRequester.getRouteCollection(routeId, startDate);

        if(routeCollection == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(routeCollection), ROUTE_COLLECTIONS_CLASS)
                        .addAction(getUpdateRouteCollectionAction(routeId, startDate))
                        .addSubEntity(getRouteCollectionPlanSubEntity(routeId, null))
                        .addLink(getRouteUpLink(routeId))
                        .addLink(getRouteCollectionSelfLink(routeId, startDate.toString()))
                , HttpStatus.OK
        );
    }

    @GetMapping(value=GET_TRUCK_COLLECTIONS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getTruckCollections(
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1") int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(TRUCK_PLATE_PATH_VAR_NAME) String truckPlate) throws Exception {
        PaginatedList<RouteCollection> routeCollections =
                routeCollectionRequester.getTruckCollections(pageNumber,rowsPerPage,truckPlate);

        String selfURIString = GET_TRUCK_COLLECTIONS_PATH.replace(TRUCK_PLATE_PATH_VAR, truckPlate);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(routeCollections.totalEntries, selfURIString, pageNumber,rowsPerPage),
                extractRouteCollectionItems(routeCollections.elements),
                new ArrayList<>(),
                of(getRouteCollectionTemplate(truckPlate)))
        );
    }

    @GetMapping(value= GET_ROUTE_COLLECTION_PLAN_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getRouteCollectionPlan (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
            @RequestParam(CONTAINER_TYPE_QUERY_PARAM) String containerType)
            throws Exception {
        PaginatedList<CollectZoneWithLocation> plan =
                collectZoneRequester.getRouteCollectionPlan(pageNumber, rowsPerPage, routeId,containerType);

        String selfURIString = GET_ROUTE_COLLECTION_PLAN_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(
                new CollectionJson(
                        new URI(selfURIWithParams),
                        getPageLinks(plan.totalEntries, selfURIString, pageNumber,rowsPerPage),
                        CollectZoneController.extractCollectZoneWithLocationItems(plan.elements, containerType),
                        new ArrayList<>(),
                        empty())
        );
    }

    /**
     * Utility Methods
     */

    private static List<Item> extractRouteItems(List<Route> routes) throws URISyntaxException {
        List<Item> items = new ArrayList<>(routes.size());
        for (Route route : routes) {
            Item item = new Item(new URI(RouteController.GET_ROUTE_PATH
                    .replace(ROUTE_ID_PATH_VAR,"" + route.routeId)))
                    .addProperty(new Property(RouteController.ROUTE_ID_FIELD_NAME, of("" + route.routeId), of(RouteController.ROUTE_ID_TITLE), empty()))
                    .addProperty(new Property(RouteController.ACTIVE_FIELD_NAME, of(route.active.equalsIgnoreCase("T")
                            ? Boolean.toString(true) : Boolean.toString(false)), of(RouteController.ACTIVE_TITLE), empty()))
                    .addProperty(new Property(RouteController.START_POINT_FIELD_NAME, of("" + route.startPoint),
                            of(RouteController.START_POINT_TITLE), empty()))
                    .addProperty(new Property(RouteController.FINISH_POINT_FIELD_NAME, of("" + route.finishPoint),
                            of(RouteController.FINISH_POINT_TITLE), empty()))
                    .addLink(getRouteCollectionsLink(route.routeId));
            items.add(item);
        }
        return items;
    }

    private static List<Item> extractRouteCollectionItems(List<RouteCollection> routeCollections)
            throws URISyntaxException {
        List<Item> items = new ArrayList<>(routeCollections.size());
        for (RouteCollection rc : routeCollections) {
            Item item = new Item(new URI(GET_ROUTE_COLLECTION_PATH.replace(ROUTE_ID_PATH_VAR, "" + rc.routeId).replace(START_DATE_PATH_VAR, rc.startDate.toString())))
                    .addProperty(new Property(ROUTE_ID_FIELD_NAME, of("" + rc.routeId), of(ROUTE_ID_TITLE), empty()))
                    .addProperty(new Property(START_DATE_FIELD_NAME, of(rc.startDate.toString()), of(START_DATE_TITLE), empty()))
                    .addProperty(new Property(FINISH_DATE_FIELD_NAME, rc.finishDate != null? of(rc.finishDate.toString()) : empty(), of(FINISH_DATE_TITLE), empty()))
                    .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, of(rc.truckPlate), of(TRUCK_PLATE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    private static CollectionLink getRouteCollectionsLink(int routeId) throws URISyntaxException {
        return new CollectionLink(Controller.ROUTE_COLLECTIONS_REL,
                new URI(RouteCollectionController.GET_ROUTE_COLLECTIONS_LIST_PATH.replace(RouteCollectionController.ROUTE_ID_PATH_VAR, "" + routeId)),
                empty(), of("Get route collections"), empty());
    }

    /**
     * Templates
     */

    private static Template getRouteCollectionTemplate() {
        return new Template()
                .addProperty(new Property(START_DATE_FIELD_NAME, empty(), of(START_DATE_TITLE), empty()))
                .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, empty(), of(TRUCK_PLATE_TITLE), empty()));
    }

    private static Template getRouteCollectionTemplate(String truckPlate) {
        return new Template()
                .addProperty(new Property(START_DATE_FIELD_NAME, empty(), of(START_DATE_TITLE), empty()))
                .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, of(truckPlate), of(TRUCK_PLATE_TITLE), empty()));
    }

    private static Template getCollectRouteTemplate() {
        return new Template()
                .addProperty(new Property(LATITUDE_FIELD_NAME, empty(), of(LATITUDE_TITLE), empty()))
                .addProperty(new Property(LONGITUDE_FIELD_NAME, empty(), of(LONGITUDE_TITLE), empty()))
                .addProperty(new Property(START_DATE_FIELD_NAME, empty(), of(START_DATE_TITLE), empty()))
                .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, empty(), of(TRUCK_PLATE_TITLE), empty()))
                .addProperty(new Property(CONTAINER_TYPE_FIELD_NAME, empty(), of(CONTAINER_TYPE_TITLE), of(ContainerMapper.CONTAINER_TYPES)));
    }

    /**
     * Siren Actions
     */

    public static final String
            UPDATE_ROUTE_COLLECTION_ACTION_NAME = "update-route-collection";

    private static SirenAction getUpdateRouteCollectionAction(int routeId, LocalDateTime startDate) throws URISyntaxException {
        return new SirenAction(
                UPDATE_ROUTE_COLLECTION_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(UPDATE_ROUTE_COLLECTION_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)
                        .replace(START_DATE_PATH_VAR, startDate.toString())),
                MediaType.APPLICATION_JSON
        )
                .addField(new Field(FINISH_DATE_FIELD_NAME, empty(), of(DATE_TIME_LOCAL_TYPE), empty(), of(FINISH_DATE_TITLE), empty()))
                .addField(new Field(TRUCK_PLATE_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(TRUCK_PLATE_TITLE), empty()));
    }


    /**
     * Sub Entities
     */

    private static SubEntity getRouteCollectionPlanSubEntity(int routeId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_ROUTE_COLLECTION_PLAN_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)),
                new String[]{ROUTE_COLLECTION_PLAN_REL}, properties, empty(), empty(), ROUTE_COLLECTION_PLAN_CLASS, COLLECTION_CLASS
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getRouteCollectionSelfLink(int routeId, String startDate) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ROUTE_COLLECTION_PATH
                                .replace(ROUTE_ID_PATH_VAR, "" + routeId)
                                .replace(START_DATE_PATH_VAR, startDate)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getRouteUpLink(int routeId) throws URISyntaxException {
        return new SirenLink(
                new URI(RouteController.GET_ROUTE_PATH.replace(RouteController.ROUTE_ID_PATH_VAR, "" + routeId)),
                SirenLink.UP_REL
        );
    }

}
