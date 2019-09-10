package pt.wastemanagement.api.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.model.Configuration;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationRequester;
import pt.wastemanagement.api.views.input.ConfigurationInput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenLink.SELF_REL;
import static pt.wastemanagement.api.views.output.siren.SirenLink.UP_REL;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;

@RestController
public class ConfigurationController  {
    //Fields
    public static final String
            CONFIGURATION_ID_FIELD_NAME = "configurationId",
            CONFIGURATION_NAME_FIELD_NAME = "configurationName";

    //Fields Title
    public static final String
            CONFIGURATION_ID_TITLE = "Configuration Id",
            CONFIGURATION_NAME_TITLE = "Configuration Name";

    //Path Vars
    public static final String
            CONFIGURATION_ID_PATH_VAR_NAME = "configuration_id",
            CONFIGURATION_ID_PATH_VAR = "{" + CONFIGURATION_ID_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            CONFIGURATIONS_PREFIX = "/configurations";

    //Paths
    public static final String
    // /configurations
            CREATE_CONFIGURATION_PATH = CONFIGURATIONS_PREFIX,
            GET_ALL_CONFIGURATIONS_PATH = CREATE_CONFIGURATION_PATH,
    // /configuration/{configuration_id}
            GET_CONFIGURATION_PATH = CREATE_CONFIGURATION_PATH + "/" + CONFIGURATION_ID_PATH_VAR,
            UPDATE_CONFIGURATION_PATH = GET_CONFIGURATION_PATH,
            DELETE_CONFIGURATION_PATH = GET_CONFIGURATION_PATH;

    private final ConfigurationRequester configurationRequester;

    public ConfigurationController(ConfigurationRequester configurationRequester) {
        this.configurationRequester = configurationRequester;
    }

    @PostMapping(CREATE_CONFIGURATION_PATH)
    public ResponseEntity createConfiguration (@RequestBody ConfigurationInput configurationInput) throws Exception {
        int configurationId = configurationRequester.createConfiguration(configurationInput.configurationName);
        List<String> headers = new ArrayList<>();
        headers.add(GET_CONFIGURATION_PATH
                .replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION, headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_CONFIGURATION_PATH)
    public ResponseEntity updateConfiguration(@PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId,
                                              @RequestBody ConfigurationInput configurationInput) throws Exception {
        configurationRequester.updateConfiguration(configurationId, configurationInput.configurationName);
        return new ResponseEntity(OK);
    }

    @DeleteMapping(DELETE_CONFIGURATION_PATH)
    public ResponseEntity deleteConfiguration (@PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId) throws Exception {
        configurationRequester.deleteConfiguration(configurationId);
        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value= GET_ALL_CONFIGURATIONS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllConfigurations (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage
    ) throws Exception {
        PaginatedList<Configuration> configurations = configurationRequester.getAllConfigurations(pageNumber, rowsPerPage);

        String selfURIString = GET_ALL_CONFIGURATIONS_PATH;
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;

        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(configurations.totalEntries, selfURIString, pageNumber, rowsPerPage),
                extractConfigurationItems(configurations.elements),
                new ArrayList<>(),
                of(getConfigurationTemplate())));
    }

    @GetMapping(value=GET_CONFIGURATION_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getConfiguration (@PathVariable(CONFIGURATION_ID_PATH_VAR_NAME) int configurationId)
            throws Exception {
        Configuration configuration = configurationRequester.getConfiguration(configurationId);

        if(configuration == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(
                new SirenOutput(of(configuration), CONFIGURATION_CLASS)
                        .addAction(getUpdateConfigurationAction(configurationId))
                        .addAction(getDeleteConfigurationAction(configurationId))
                        .addSubEntity(getConfigurationCommunicationsListSubEntity(configurationId, null))
                        .addLink(getCommunicationSelfLink(configurationId))
                        .addLink(getCommunicationsListUpLink()),
                OK);
    }

    /**
     * Utility methods
     */

    private static List<Item> extractConfigurationItems(List<Configuration> configurations) throws URISyntaxException {
        List<Item> items = new ArrayList<>(configurations.size());
        for (Configuration c : configurations) {
            Item item = new Item(
                    new URI(GET_CONFIGURATION_PATH
                            .replace(CONFIGURATION_ID_PATH_VAR, "" + c.configurationId)
                    ))
                    .addProperty(new Property(CONFIGURATION_ID_FIELD_NAME, of("" + c.configurationId), of(CONFIGURATION_ID_TITLE), empty()))
                    .addProperty(new Property(CONFIGURATION_NAME_FIELD_NAME, of(c.configurationName), of(CONFIGURATION_NAME_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    private static Template getConfigurationTemplate() {
        return new Template().addProperty(new Property(CONFIGURATION_NAME_FIELD_NAME, empty(), of(CONFIGURATION_NAME_TITLE), empty()));
    }



    /**
     * Siren Actions
     */

    public static final String
            UPDATE_CONFIGURATION_ACTION_NAME = "update-configuration",
            DELETE_CONFIGURATION_ACTION_NAME = "delete-configuration";

   private static SirenAction getUpdateConfigurationAction (int configurationId) throws URISyntaxException {
        return new SirenAction(UPDATE_CONFIGURATION_ACTION_NAME, empty(), PUT,
                new URI(GET_CONFIGURATION_PATH.replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                APPLICATION_JSON
        )
                .addField(new Field(CONFIGURATION_NAME_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(CONFIGURATION_NAME_TITLE), empty()));
   }

    private static SirenAction getDeleteConfigurationAction (int configurationId) throws URISyntaxException {
        return new SirenAction(DELETE_CONFIGURATION_ACTION_NAME, empty(), DELETE,
                new URI(DELETE_CONFIGURATION_PATH.replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                ALL
        );
    }

    /**
     * Sub Entity
     */

    private static SubEntity getConfigurationCommunicationsListSubEntity(int configurationId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(ConfigurationCommunicationController.GET_CONFIGURATION_COMMUNICATIONS_LIST_PATH
                        .replace(ConfigurationCommunicationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                new String[]{CONFIGURATION_COMMUNICATIONS_LIST_REL}, properties, empty(), empty(), CONFIGURATION_COMMUNICATION_LIST_CLASS, COLLECTION_CLASS
        );
    }

    /**
     * Siren Links
     */
    private static SirenLink getCommunicationSelfLink (int configurationId) throws URISyntaxException {
        return new SirenLink(new URI(GET_CONFIGURATION_PATH.replace(CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                SELF_REL
        );
    }

    private static SirenLink getCommunicationsListUpLink () throws URISyntaxException {
        return new SirenLink(new URI(GET_ALL_CONFIGURATIONS_PATH),
                UP_REL
        );
    }
}
