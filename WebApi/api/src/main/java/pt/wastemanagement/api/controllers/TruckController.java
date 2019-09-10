package pt.wastemanagement.api.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.model.Truck;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.TruckRequester;
import pt.wastemanagement.api.views.output.GetTuck;
import pt.wastemanagement.api.views.input.TruckInput;
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
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static pt.wastemanagement.api.controllers.RouteCollectionController.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class TruckController  {
    //Fields
    public static final String
            TRUCK_PLATE_FIELD_NAME = "truckPlate",
            ACTIVE_FIELD_NAME = "active";

    //Fields Titles
    public static final String
            TRUCK_PLATE_TITLE = "Truck Plate",
            ACTIVE_TITLE = "Active";

    //Path Vars
    public static final String
            TRUCK_PLATE_PATH_VAR_NAME = "truck_plate",
            TRUCK_PLATE_PATH_VAR = "{" + TRUCK_PLATE_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            TRUCKS_PREFIX = "/trucks";

    //Paths
    public static final String
            CREATE_TRUCK_PATH = TRUCKS_PREFIX,
            DEACTIVATE_TRUCK_PATH = CREATE_TRUCK_PATH + DEACTIVATE_PREFIX,
            ACTIVATE_TRUCK_PATH = CREATE_TRUCK_PATH + ACTIVATE_PREFIX,
            GET_ALL_TRUCKS_PATH = TRUCKS_PREFIX,
            GET_TRUCK_PATH = TRUCKS_PREFIX + "/" + TRUCK_PLATE_PATH_VAR;

    private final TruckRequester truckRequester;
    private static final Logger log = LoggerFactory.getLogger(TruckController.class);

    public TruckController(TruckRequester truckRequester) {
        this.truckRequester = truckRequester;
    }

    @PostMapping(CREATE_TRUCK_PATH)
    public ResponseEntity createTruck (@RequestBody TruckInput truckInput) throws Exception {
        if(!truckInput.truckPlate.matches("\\w{2}-\\w{2}-\\w{2}")) //This implementation varies from country
            throw new IllegalArgumentException("Truck registration plate has an invalid format");

        truckRequester.createTruck(truckInput.truckPlate);
        List<String> headers = new ArrayList<>();
        headers.add(GET_TRUCK_PATH
                .replace(TRUCK_PLATE_PATH_VAR, truckInput.truckPlate));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION, headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(DEACTIVATE_TRUCK_PATH)
    public ResponseEntity deactivateTruck (@PathVariable(TRUCK_PLATE_PATH_VAR_NAME) String truckPlate) throws Exception {

        truckRequester.deactivateTruck(truckPlate);
        return new ResponseEntity(OK);
    }

    @PutMapping(ACTIVATE_TRUCK_PATH)
    public ResponseEntity activateTruck (@PathVariable(TRUCK_PLATE_PATH_VAR_NAME) String truckPlate) throws Exception {
        truckRequester.activateTruck(truckPlate);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_ALL_TRUCKS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllTrucks(
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(value = SHOW_INACTIVE_QUERY_PARAM,  defaultValue = "false") boolean showInactive) throws Exception {
        PaginatedList<Truck> trucks = truckRequester.getAllTrucks(pageNumber, rowsPerPage, showInactive);

        String selfURIString = GET_ALL_TRUCKS_PATH + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIString),
                getPageLinks(trucks.totalEntries, GET_ALL_TRUCKS_PATH, pageNumber, rowsPerPage, showInactive),
                extractTruckItems(trucks),
                getShowInactiveQueries(GET_ALL_TRUCKS_PATH, rowsPerPage, showInactive),
                of(getTruckTemplate())
        ));
    }

    @GetMapping(value = GET_TRUCK_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getTruck(@PathVariable(TRUCK_PLATE_PATH_VAR_NAME) String truckPlate)
            throws Exception {
        Truck truck = truckRequester.getTruck(truckPlate);

        if (truck == null) return new ResponseEntity(NOT_FOUND);

        GetTuck getTuck = new GetTuck(truck);

        return new ResponseEntity(
                new SirenOutput(of(getTuck), TRUCK_CLASS)
                        .addAction(getDeactivateTruckAction(truckPlate))
                        .addAction(getActivateTruckAction(truckPlate))
                        .addSubEntity(getTruckCollectsSubEntity(truckPlate, null))
                        .addLink(getTruckSelfLink(truckPlate))
                        .addLink(getTrucksListUpLink()),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractTruckItems(PaginatedList<Truck> trucks) throws URISyntaxException {
        List<Item> items = new ArrayList<>(trucks.elements.size());
        for (Truck t : trucks.elements){
            Item item = new Item(new URI(GET_TRUCK_PATH.replace(TRUCK_PLATE_PATH_VAR, t.registrationPlate)))
                    .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, of(t.registrationPlate), of(TRUCK_PLATE_TITLE), empty()))
                    .addProperty(new Property(ACTIVE_FIELD_NAME, of(t.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)),
                            of(ACTIVE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getTruckTemplate(){
        return new Template()
                .addProperty(new Property(TRUCK_PLATE_FIELD_NAME, empty(), of(TRUCK_PLATE_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            DEACTIVATE_TRUCK_ACTION_NAME = "deactivate-truck",
            ACTIVATE_TRUCK_ACTION_NAME = "activate-truck";

    private static SirenAction getDeactivateTruckAction(String truckPlate) throws URISyntaxException {
        return new SirenAction(DEACTIVATE_TRUCK_ACTION_NAME, empty(), PUT,
                new URI(DEACTIVATE_TRUCK_PATH.replace(TRUCK_PLATE_PATH_VAR, truckPlate)),
                ALL
        );
    }

    private static SirenAction getActivateTruckAction(String truckPlate) throws URISyntaxException {
        return new SirenAction(ACTIVATE_TRUCK_ACTION_NAME, empty(), PUT,
                new URI(ACTIVATE_TRUCK_PATH.replace(TRUCK_PLATE_PATH_VAR, truckPlate)),
                ALL
        );
    }

    /**
     * Sub Entities
     */

    private static SubEntity getTruckCollectsSubEntity(String truckPlate, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(GET_TRUCK_COLLECTIONS_PATH.replace(RouteCollectionController.TRUCK_PLATE_PATH_VAR, truckPlate)),
                new String[]{TRUCK_COLLECTS_REL}, properties, empty(), empty(), TRUCK_COLLECTS_CLASS, COLLECTION_CLASS
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getTruckSelfLink(String truckPlate) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_TRUCK_PATH.replace(TRUCK_PLATE_PATH_VAR, truckPlate)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getTrucksListUpLink() throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ALL_TRUCKS_PATH),
                SirenLink.UP_REL
        );
    }
}
