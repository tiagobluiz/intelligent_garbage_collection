package pt.wastemanagement.api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationCommunicationRequester;
import pt.wastemanagement.api.views.input.ConfigurationCommunicationInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenLink;
import pt.wastemanagement.api.views.output.siren.SirenOutput;
import pt.wastemanagement.api.views.output.siren.SubEntity;

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
public class ConfigurationCommunicationController {
    //Fields
    public static final String
            CONFIGURATION_ID_FIELD_NAME = "configurationId",
            COMMUNICATION_ID_FIELD_NAME = "communicationId",
            COMMUNICATION_NAME_FIELD_NAME = "communicationDesignation",
            VALUE_FIELD_NAME = "value";

    //Fields Titles
    public static final String
            CONFIGURATION_ID_TITLE = "Configuration Id",
            COMMUNICATION_ID_TITLE = "Communication Id",
            COMMUNICATION_NAME_TITLE = "Communication Designation",
            VALUE_TITLE = "Value";

    //Path Vars
    public static final String
            CONFIGURATION_ID_PATH_VAR_NAME = ConfigurationController.CONFIGURATION_ID_PATH_VAR_NAME,
            CONFIGURATION_ID_PATH_VAR = ConfigurationController.CONFIGURATION_ID_PATH_VAR,
            COMMUNICATION_ID_PATH_VAR_NAME = CommunicationController.COMMUNICATION_ID_PATH_VAR_NAME,
            COMMUNICATION_ID_PATH_VAR = CommunicationController.COMMUNICATION_ID_PATH_VAR;

    //Path partitions
    private static final String
            CONFIGURATIONS_PREFIX = ConfigurationController.GET_CONFIGURATION_PATH,
            COMMUNICATIONS_PREFIX = "/communications";

    //Paths
    public static final String
            GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH = CONFIGURATIONS_PREFIX + COMMUNICATIONS_PREFIX,
            GET_CONFIGURATION_COMMUNICATION_PATH = GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH + "/" + COMMUNICATION_ID_PATH_VAR,
            ASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_PATH = GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH,
            DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_PATH = GET_CONFIGURATION_COMMUNICATION_PATH;

    private final ConfigurationCommunicationRequester configurationCommunicationRequester;

    public ConfigurationCommunicationController(ConfigurationCommunicationRequester configurationCommunicationRequester) {
        this.configurationCommunicationRequester = configurationCommunicationRequester;
    }

    @PostMapping(ASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_PATH)
    public ResponseEntity associateCommunicationToTheConfiguration (
            @PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId,
            @RequestBody ConfigurationCommunicationInput configCommInput) throws Exception {
        if(configCommInput.value < -15)
            throw new IllegalArgumentException("The value for that configuration must be greater than -15");

        configurationCommunicationRequester
                .associateCommunicationToTheConfiguration(configurationId, configCommInput.communicationId, configCommInput.value);
        List<String> headers = new ArrayList<>();
        headers.add(GET_CONFIGURATION_COMMUNICATION_PATH
                .replace(CONFIGURATION_ID_PATH_VAR,"" + configurationId)
                .replace(COMMUNICATION_ID_PATH_VAR, "" + configCommInput.communicationId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @DeleteMapping(DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_PATH)
    public ResponseEntity disassociateCommunicationToTheConfiguration (
            @PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId,
            @PathVariable(COMMUNICATION_ID_PATH_VAR_NAME) int communicationId) throws Exception {
        configurationCommunicationRequester.disassociateCommunicationToTheConfiguration(configurationId, communicationId);
        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value=GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getConfigurationCommunicationList (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId) throws Exception {

        PaginatedList<ConfigurationCommunication> configurationCommunication =
                configurationCommunicationRequester.getConfigurationCommunicationsList(pageNumber, rowsPerPage, configurationId);

        String selfURIString = GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH
                .replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(configurationCommunication.totalEntries, selfURIString, pageNumber, rowsPerPage),
                extractConfigurationCommunicationItems(configurationCommunication.elements),
                new ArrayList<>(),
                of(getConfigurationCommunicationTemplate())
        ));
    }

    @GetMapping(value=GET_CONFIGURATION_COMMUNICATION_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getConfigurationCommunication (
            @PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId,
            @PathVariable(COMMUNICATION_ID_PATH_VAR_NAME) int communicationId
    ) throws Exception {
        ConfigurationCommunication configurationCommunication =
                configurationCommunicationRequester.getConfigurationCommunication(configurationId, communicationId);

        if(configurationCommunication == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(configurationCommunication), CONFIGURATION_COMMUNICATION_CLASS)
                        .addAction(getDisassociateCommunicationConfigurationAction(configurationId, communicationId))
                        .addSubEntity(getCommunicationSubEntity(communicationId, null))
                        .addSubEntity(getConfigurationSubEntity(configurationId, null))
                        .addLink(getConfigurationCommunicationSelfLink(configurationId, communicationId))
                        .addLink(getConfigurationCommunicationsListUpLink(configurationId)),
                OK);
    }

    /**
     * Utility Methods
     */

    private static List<Item> extractConfigurationCommunicationItems(List<ConfigurationCommunication> configurationCommunication)
            throws URISyntaxException {
        List<Item> items = new ArrayList<>(configurationCommunication.size());
        for (ConfigurationCommunication cc : configurationCommunication){
            Item item = new Item(
                    new URI(
                            GET_CONFIGURATION_COMMUNICATION_PATH
                                    .replace(CONFIGURATION_ID_PATH_VAR, "" + cc.configurationId)
                                    .replace(COMMUNICATION_ID_PATH_VAR, "" + cc.communicationId)
                    ))
                    .addProperty(new Property(CONFIGURATION_ID_FIELD_NAME, of("" + cc.configurationId), of(CONFIGURATION_ID_TITLE), empty()))
                    .addProperty(new Property(COMMUNICATION_ID_FIELD_NAME, of("" + cc.communicationId), of(COMMUNICATION_ID_TITLE), empty()))
                    .addProperty(new Property(COMMUNICATION_NAME_FIELD_NAME, of("" + cc.communicationDesignation), of(COMMUNICATION_NAME_TITLE), empty()))
                    .addProperty(new Property(VALUE_FIELD_NAME, of("" + cc.value), of(VALUE_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Templates
     */

    private static Template getConfigurationCommunicationTemplate(){
        return new Template()
                .addProperty(new Property(COMMUNICATION_ID_FIELD_NAME, empty(), of(COMMUNICATION_ID_TITLE), empty()))
                .addProperty(new Property(VALUE_FIELD_NAME, empty(), of(VALUE_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    public static final String
            DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_ACTION_NAME = "disassociate-communication-configuration";
    private static SirenAction getDisassociateCommunicationConfigurationAction
            (int configurationId, int communicationId) throws URISyntaxException {
        return new SirenAction(DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_ACTION_NAME, empty(),
                DELETE,
                new URI(DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION_PATH
                        .replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)
                        .replace(COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                ALL
        );
    }

    /**
     * Sub Entities
     */

    private static SubEntity getConfigurationSubEntity (int configurationId, Object properties)
            throws URISyntaxException {
        return new SubEntity(
                new URI(ConfigurationController.GET_CONFIGURATION_PATH
                        .replace(ConfigurationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                new String[]{CONFIGURATION_REL}, properties, empty(), empty(),
                CONFIGURATION_CLASS
        );
    }

    private static SubEntity getCommunicationSubEntity(int communicationId, Object properties)
            throws URISyntaxException {
        return new SubEntity(
                new URI(CommunicationController.GET_COMMUNICATION_PATH
                        .replace(CommunicationController.COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                new String[]{COMMUNICATION_REL}, properties, empty(), empty(),
                COMMUNICATION_CLASS
        );
    }



    /**
     * Siren Links
     */

    private static SirenLink getConfigurationCommunicationSelfLink(int configurationId, int communicationId)
            throws URISyntaxException {
        return new SirenLink(
                new URI(GET_CONFIGURATION_COMMUNICATION_PATH
                        .replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)
                        .replace(COMMUNICATION_ID_PATH_VAR, "" + communicationId)),
                SELF_REL
        );
    }

    private static SirenLink getConfigurationCommunicationsListUpLink(int configurationId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH
                        .replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                UP_REL
        );
    }
}
