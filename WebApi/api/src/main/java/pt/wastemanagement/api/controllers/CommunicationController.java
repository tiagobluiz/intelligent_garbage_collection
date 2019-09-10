package pt.wastemanagement.api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.Communication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CommunicationRequester;
import pt.wastemanagement.api.views.input.CommunicationInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.Field;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenLink;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenLink.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;


@RestController
public class CommunicationController {
    //Fields
    public static final String
            COMMUNICATION_ID_FIELD_NAME = "communicationId",
            COMMUNICATION_NAME_FIELD_NAME = "communicationDesignation";

    //Fields Titles
    public static final String
            COMMUNICATION_ID_TITLE = "Communication Id",
            COMMUNICATION_NAME_TITLE = "Communication Designation";

    //Path Vars
    public static final String
            COMMUNICATION_ID_PATH_VAR_NAME = "communication_id",
            COMMUNICATION_ID_PATH_VAR = "{" + COMMUNICATION_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            COMMUNICATION_PREFIX = "/communications";

    //Path
    public static final String
    // /communications
            CREATE_COMMUNICATION_PATH = COMMUNICATION_PREFIX,
            GET_ALL_COMMUNICATIONS_PATH = CREATE_COMMUNICATION_PATH,
    // /communications/{communication_id}
            GET_COMMUNICATION_PATH = CREATE_COMMUNICATION_PATH + "/" + COMMUNICATION_ID_PATH_VAR,
            UPDATE_COMMUNICATION_PATH = GET_COMMUNICATION_PATH,
            DELETE_COMMUNICATION_PATH = GET_COMMUNICATION_PATH;

    private final CommunicationRequester communicationRequester;

    public CommunicationController(CommunicationRequester communicationRequester) {
        this.communicationRequester = communicationRequester;
    }

    @PostMapping(CREATE_COMMUNICATION_PATH)
    public ResponseEntity createCommunication (@RequestBody CommunicationInput communicationInput) throws Exception {
        int communicationId = communicationRequester.createCommunication(communicationInput.communicationDesignation);
        List<String> headers = new ArrayList<>();
        headers.add(GET_COMMUNICATION_PATH.replace(COMMUNICATION_ID_PATH_VAR,"" + communicationId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_COMMUNICATION_PATH)
    public ResponseEntity updateCommunication (@PathVariable(COMMUNICATION_ID_PATH_VAR_NAME) int communicationId,
                                               @RequestBody CommunicationInput communicationInput) throws Exception {
        communicationRequester.updateCommunication(communicationId, communicationInput.communicationDesignation);
        return new ResponseEntity(OK);
    }

    @DeleteMapping(DELETE_COMMUNICATION_PATH)
    public ResponseEntity deleteCommunication (@PathVariable(COMMUNICATION_ID_PATH_VAR_NAME) int communicationId) throws Exception {
        communicationRequester.deleteCommunication(communicationId);
        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value=GET_ALL_COMMUNICATIONS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllCommunications (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage) throws Exception {
        PaginatedList<Communication> communications = communicationRequester.getAllCommunications(pageNumber, rowsPerPage);

        String selfURIWithParams = GET_ALL_COMMUNICATIONS_PATH + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(communications.totalEntries, GET_ALL_COMMUNICATIONS_PATH,pageNumber, rowsPerPage),
                extractCommunicationItems(communications.elements),
                new ArrayList<>(),
                of(getCommunicationTemplate())));
    }



    @GetMapping(value=GET_COMMUNICATION_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getCommunication (@PathVariable(COMMUNICATION_ID_PATH_VAR_NAME) int communicationId) throws Exception {
        Communication communication =  communicationRequester.getCommunication(communicationId);

        if(communication == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(communication), COMMUNICATION_CLASS)
                        .addAction(getUpdateCommunicationAction(communicationId))
                        .addAction(getDeleteCommunicationAction(communicationId))
                        .addLink(getCommunicationSelfLink(communicationId))
                        .addLink(getCommunicationsListUpLink()),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractCommunicationItems(List<Communication> communications) throws URISyntaxException {
        List<Item> items = new ArrayList<>(communications.size());
        for (Communication c :communications) {
            Item item = new Item(new URI(GET_COMMUNICATION_PATH.replace(COMMUNICATION_ID_PATH_VAR, "" + c.communicationId)))
                    .addProperty(new Property(COMMUNICATION_ID_FIELD_NAME, of("" + c.communicationId), of(COMMUNICATION_ID_TITLE), empty()))
                    .addProperty(new Property(COMMUNICATION_NAME_FIELD_NAME, of(c.communicationDesignation), of(COMMUNICATION_NAME_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getCommunicationTemplate() {
        return new Template()
                .addProperty(new Property(COMMUNICATION_NAME_FIELD_NAME, empty(), of(COMMUNICATION_NAME_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            UPDATE_COMMUNICATION_ACTION_NAME = "update-communication",
            DELETE_COMMUNICATION_ACTION_NAME = "delete-communication";

    private static SirenAction getUpdateCommunicationAction (int communicationId) throws URISyntaxException {
        return new SirenAction(UPDATE_COMMUNICATION_ACTION_NAME, empty(), PUT,
                new URI(UPDATE_COMMUNICATION_PATH.replace(COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                APPLICATION_JSON
        )
                .addField(new Field(COMMUNICATION_NAME_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(COMMUNICATION_NAME_TITLE), empty()));
    }

    private static SirenAction getDeleteCommunicationAction (int communicationId) throws URISyntaxException {
        return new SirenAction(DELETE_COMMUNICATION_ACTION_NAME, empty(), DELETE,
                new URI(DELETE_COMMUNICATION_PATH.replace(COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                ALL
        );
    }

    /**
     * Siren Links
     */
    private static SirenLink getCommunicationSelfLink (int communicationId) throws URISyntaxException {
        return new SirenLink(new URI(GET_COMMUNICATION_PATH.replace(COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                SELF_REL
        );
    }

    private static SirenLink getCommunicationsListUpLink () throws URISyntaxException {
        return new SirenLink(new URI(GET_ALL_COMMUNICATIONS_PATH),
                UP_REL
        );
    }
}
