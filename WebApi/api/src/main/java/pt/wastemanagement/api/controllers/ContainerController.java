package pt.wastemanagement.api.controllers;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.mappers.ConfigurationCommunicationMapper;
import pt.wastemanagement.api.mappers.ContainerMapper;
import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.model.Container;
import pt.wastemanagement.api.model.functions.ContainerStatistics;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationCommunicationRequester;
import pt.wastemanagement.api.requesters.ContainerRequester;
import pt.wastemanagement.api.views.input.ContainerConfigurationInput;
import pt.wastemanagement.api.views.input.ContainerInput;
import pt.wastemanagement.api.views.input.ContainerLocalizationInput;
import pt.wastemanagement.api.views.input.ContainerReadsInput;
import pt.wastemanagement.api.views.output.GetContainer;
import pt.wastemanagement.api.views.output.OccupationInRangeReturn;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.siren.*;

import java.io.*;
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
public class ContainerController  {
    //Configurations
    private static final String
            READS_LOG_DIRECTORY = "/var/log/reads",
            READS_LOG_FILE_NAME_TEMPLATE = READS_LOG_DIRECTORY + "container_%s_reads.txt";
    public static final int
            SIGFOX_RESPONSE_LENGTH = 16;
    //Fields
    public static final String
            CONTAINER_ID_FIELD_NAME = "containerId",
            IOT_ID_FIELD_NAME = "iotId",
            ACTIVE_FIELD_NAME = "active",
            LATITUDE_FIELD_NAME = "latitude",
            LONGITUDE_FIELD_NAME = "longitude",
            CONTAINER_TYPE_FIELD_NAME = "containerType",
            LAST_READ_DATE_FIELD_NAME = "lastReadDate",
            HEIGHT_FIELD_NAME = "height",
            BATTERY_FIELD_NAME = "battery",
            OCCUPATION_FIELD_NAME = "occupation",
            TEMPERATURE_FIELD_NAME = "temperature",
            COLLECT_ZONE_ID_FIELD_NAME = "collectZoneId",
            CONFIGURATION_ID_FIELD_NAME = "configurationId",
    //Container statistics
            NUMBER_OF_COLLECTS_FIELD_NAME = "numCollects",
            NUMBER_OF_WASHES_FIELD_NAME = "numWashes";

    //Fields Titles
    public static final String
            CONTAINER_ID_TITLE = "Container Id",
            IOT_ID_TITLE = "Iot Id",
            ACTIVE_TITLE = "Active",
            LATITUDE_TITLE = "Latitude",
            LONGITUDE_TITLE = "Longitude",
            CONTAINER_TYPE_TITLE = "Container Type",
            HEIGHT_TITLE = "Height",
            LAST_READ_DATE_TITLE = "Last Read Date",
            BATTERY_TITLE = "Battery",
            OCCUPATION_TITLE = "Occupation",
            TEMPERATURE_TITLE = "Temperature",
            COLLECT_ZONE_ID_TITLE = "Collect Zone Id",
            CONFIGURATION_ID_TITLE = "Configuration Id",
    //Container statistics
            NUMBER_OF_COLLECTS_TITLE = "Number of Collects",
            NUMBER_OF_WASHES_TITLE = "Number of Washes";

    //Path Vars
    public static final String
            CONTAINER_ID_PATH_VAR_NAME = "container_id",
            CONTAINER_ID_PATH_VAR = "{" + CONTAINER_ID_PATH_VAR_NAME + "}",
            IOT_ID_PATH_VAR_NAME = "iot_id",
            IOT_ID_PATH_VAR = "{" + IOT_ID_PATH_VAR_NAME + "}",
            COLLECT_ZONE_ID_PATH_VAR_NAME = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR_NAME,
            COLLECT_ZONE_ID_PATH_VAR = CollectZoneController.COLLECT_ZONE_ID_PATH_VAR,
            ROUTE_ID_PATH_VAR_NAME = RouteController.ROUTE_ID_PATH_VAR_NAME,
            ROUTE_ID_PATH_VAR = RouteController.ROUTE_ID_PATH_VAR;

    //Path partitions
    private static final String
            CONTAINERS_PREFIX = "/containers",
            LOCALIZATION_PREFIX = "/localization",
            CONFIGURATION_PREFIX = "/configuration",
            READS_PREFIX = "/reads",
            ROUTES_PREFIX = RouteController.GET_ROUTE_PATH,
            OCCUPATION_IN_RANGE_PREFIX = "/occupation-in-range",
            COLLECT_ZONES_PREFIX = "/collect-zones";

    //Query Parameters
    public static final String
            MIN_RANGE_QUERY_PARAM = "min",
            MAX_RANGE_QUERY_PARAM = "max";

    //Path
    public static final String
            CREATE_CONTAINER_PATH = COLLECT_ZONES_PREFIX + "/" + COLLECT_ZONE_ID_PATH_VAR +
                    CONTAINERS_PREFIX,
            GET_CONTAINER_PATH = CONTAINERS_PREFIX + "/" + CONTAINER_ID_PATH_VAR,
            UPDATE_CONTAINER_CONFIGURATION_PATH = GET_CONTAINER_PATH + CONFIGURATION_PREFIX,
            UPDATE_CONTAINER_LOCALIZATION_PATH = GET_CONTAINER_PATH + LOCALIZATION_PREFIX,
            UPDATE_CONTAINER_READS_PATH = CONTAINERS_PREFIX + "/" + IOT_ID_PATH_VAR + READS_PREFIX,
            DEACTIVATE_CONTAINER_PATH = GET_CONTAINER_PATH + DEACTIVATE_PREFIX,
            ACTIVATE_CONTAINER_PATH = GET_CONTAINER_PATH + ACTIVATE_PREFIX,
            GET_COLLECT_ZONE_CONTAINERS_PATH = COLLECT_ZONES_PREFIX + "/" + COLLECT_ZONE_ID_PATH_VAR +
                    CONTAINERS_PREFIX,
            GET_ROUTE_CONTAINERS_PATH = ROUTES_PREFIX + CONTAINERS_PREFIX,
            GET_CONTAINERS_IN_RANGE_PATH = CONTAINERS_PREFIX + OCCUPATION_IN_RANGE_PREFIX,
            GET_CONTAINERS_IN_RANGE_PATH_WITH_QUERY_PARAMS = CONTAINERS_PREFIX + OCCUPATION_IN_RANGE_PREFIX +
                    "?"+ MIN_RANGE_QUERY_PARAM + "={" + MIN_RANGE_QUERY_PARAM + "}&" + MAX_RANGE_QUERY_PARAM +"={" + MAX_RANGE_QUERY_PARAM +"}",
            GET_ROUTE_CONTAINERS_IN_RANGE_PATH = GET_ROUTE_CONTAINERS_PATH + OCCUPATION_IN_RANGE_PREFIX;

    private final ContainerRequester containerRequester;
    private final ConfigurationCommunicationRequester configurationCommunicationRequester;
    private static final Logger log = LoggerFactory.getLogger(ContainerController.class);

    public ContainerController(ContainerRequester containerRequester, ConfigurationCommunicationRequester configurationCommunicationRequester) {
        this.containerRequester = containerRequester;
        this.configurationCommunicationRequester = configurationCommunicationRequester;
    }

    @PostMapping(CREATE_CONTAINER_PATH)
    public ResponseEntity createContainer (@PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId,
                                           @RequestBody ContainerInput containerInput) throws Exception {
        if(containerInput.longitude < -180 || containerInput.longitude >  180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if(containerInput.latitude < -90 || containerInput.latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        if(containerInput.height <= 0)
            throw new IllegalArgumentException("Height must be a positive number");
        if(!isContainerTypeValid(containerInput.containerType)) {
            log.info("Container type received was invalid: {}", containerInput.containerType);
            throw new IllegalArgumentException("Container type is invalid. Verify if the type is one of these: " + ContainerMapper.CONTAINER_TYPES.toString());
        }

        int containerId = containerRequester.createContainer(
                containerInput.iotId, containerInput.latitude, containerInput.longitude, containerInput.height,
                containerInput.containerType, collectZoneId, containerInput.configurationId
        );
        List<String> headers = new ArrayList<>();
        headers.add(GET_CONTAINER_PATH.replace(CONTAINER_ID_PATH_VAR,"" + containerId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(UPDATE_CONTAINER_CONFIGURATION_PATH)
    public ResponseEntity updateContainerConfiguration (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                                        @RequestBody ContainerConfigurationInput configsInput) throws Exception {
        if(!isContainerTypeValid(configsInput.containerType)) {
            log.info("Container type received was invalid: {}", configsInput.containerType);
            throw new IllegalArgumentException("Container type is invalid. Verify if the type is one of these: " + ContainerMapper.CONTAINER_TYPES.toString());
        }

        containerRequester.updateContainerConfiguration(
                containerId, configsInput.iotId, configsInput.height,
                configsInput.containerType, configsInput.configurationId
        );
        return new ResponseEntity(OK);
    }

    @PutMapping(UPDATE_CONTAINER_LOCALIZATION_PATH)
    public ResponseEntity updateContainerLocalization (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId,
                                                       @RequestBody ContainerLocalizationInput localizationInput) throws Exception {
        if(localizationInput.collectZoneId < 0)
            throw new IllegalArgumentException("Invalid collect zone identifier, it must be a positive number");
        if(localizationInput.longitude < -180 || localizationInput.longitude >  180)
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        if(localizationInput.latitude < -90 || localizationInput.latitude > 90)
            throw new IllegalArgumentException("Latitude must be between -90 and 90");

        containerRequester.updateContainerLocalization(
                containerId, localizationInput.latitude, localizationInput.longitude, localizationInput.collectZoneId
        );
        return new ResponseEntity(OK);
    }

    /**
     * The request interpretation is dependent on how the IOT device sends the data.
     * This implementation is specific for the device implemented for this project.
     *
     * This method, due to Sigfox limitations, needs to return the IOT configuration, in order
     * to allow the device to update his configuration
     */
    @PutMapping(value = UPDATE_CONTAINER_READS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateContainerReads (@PathVariable(IOT_ID_PATH_VAR_NAME) String iotId,
                                                @RequestBody ContainerReadsInput readsInput) throws Exception {
        boolean storeData = true;
        if(readsInput.data != null && !readsInput.data.equals(""))
            storeData = parseBodyData(readsInput);

        if(storeData && readsInput.temperature <-15 && readsInput.temperature != -100)
            throw new IllegalArgumentException("Temperature must be higher than -15. For temperature read errors, send -100ºC.");
        if(storeData && readsInput.occupation < -1)
            throw new IllegalArgumentException("Occupation must be -1 if an error occurred or between 0 and 100 if a read was successful");
        if(storeData && readsInput.battery < -1)
            throw new IllegalArgumentException("Battery must be -1 if an error occurred or between 0 and 100 if a read was successful");

        Container container = containerRequester.getContainerByIotId(iotId);
        if(container == null) return new ResponseEntity(NOT_FOUND);

        if(storeData){
            containerRequester.updateContainerReads(iotId, readsInput.battery, readsInput.occupation, readsInput.temperature);
            writeToFile(container.containerId, readsInput);
        }

        /**
         * The reason why is created a list, is to allow this method to expand in the future
         */
        List<ConfigurationCommunication> configurationCommunicationList = new ArrayList<>();
        configurationCommunicationList.add(configurationCommunicationRequester.getConfigurationCommunicationByName(container.configurationId, ConfigurationCommunicationMapper.COMMUNICATION_INTERVAL_COMMUNICATION_NAME));

        String responseTemplate =  "{\"%s\":{\"downlinkData\":\"%s\"}}";

        return ResponseEntity.status(OK)
                .body(String.format(responseTemplate, iotId,
                        convertContainerConfigurationsIntoHexa(container.height, configurationCommunicationList)));
    }

    @PutMapping(DEACTIVATE_CONTAINER_PATH)
    public ResponseEntity deactivateContainer (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId) throws Exception {
        containerRequester.deactivateContainer(containerId);
        return new ResponseEntity(OK);
    }

    @PutMapping(ACTIVATE_CONTAINER_PATH)
    public ResponseEntity activateContainer (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId) throws Exception {
        containerRequester.activateContainer(containerId);
        return new ResponseEntity(OK);
    }

    @GetMapping(value=GET_COLLECT_ZONE_CONTAINERS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getCollectZoneContainers (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(value = SHOW_INACTIVE_QUERY_PARAM,  defaultValue = "false") boolean showInactive,
            @PathVariable(COLLECT_ZONE_ID_PATH_VAR_NAME) int collectZoneId) throws Exception {
        PaginatedList<Container> containers =
                containerRequester.getCollectZoneContainers(pageNumber, rowsPerPage, collectZoneId, showInactive);

        String selfURIString = GET_COLLECT_ZONE_CONTAINERS_PATH.replace(COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(containers.totalEntries, selfURIString,pageNumber, rowsPerPage, showInactive),
                extractContainerItems(containers.elements),
                getShowInactiveQueries(selfURIString, rowsPerPage, showInactive),
                of(getContainerTemplate())
        ));
    }

    @GetMapping(value=GET_ROUTE_CONTAINERS_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getRouteContainers (
            @RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
            @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage,
            @RequestParam(value = SHOW_INACTIVE_QUERY_PARAM,  defaultValue = "false") boolean showInactive,
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId) throws Exception {
        PaginatedList<Container> containers =
                containerRequester.getRouteContainers(pageNumber, rowsPerPage, routeId, showInactive);

        String selfURIString = GET_ROUTE_CONTAINERS_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId);
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage + "&" + SHOW_INACTIVE_QUERY_PARAM + "=" + showInactive;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(containers.totalEntries, selfURIString,pageNumber, rowsPerPage, showInactive),
                extractContainerItems(containers.elements),
                getShowInactiveQueries(selfURIString, rowsPerPage, showInactive),
                empty()
        ));
    }

    @GetMapping(value=GET_CONTAINER_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getContainer (@PathVariable(CONTAINER_ID_PATH_VAR_NAME) int containerId)
            throws Exception {
        Container containerInfo = containerRequester.getContainerInfo(containerId);
        ContainerStatistics containerStatistics = containerRequester.getContainerStatistics(containerId);

        if(containerInfo == null && containerStatistics == null) return new ResponseEntity(NOT_FOUND);

        GetContainer getContainer = new GetContainer(containerInfo, containerStatistics);

        return new ResponseEntity(new SirenOutput(of(getContainer), CONTAINER_CLASS)
                        .addAction(getUpdateContainerConfigurationAction(containerId))
                        .addAction(getUpdateContainerLocalizationAction(containerId))
                        .addAction(getUpdateContainerReadsAction(containerId))
                        .addAction(getDeactivateContainerAction(containerId))
                        .addAction(getActivateContainerAction(containerId))
                        .addSubEntity(getConfigurationSubEntity(containerInfo.configurationId, null))
                        .addSubEntity(getCollectZoneSubEntity(containerInfo.collectZoneId, null))
                        .addSubEntity(getWashesSubEntity(containerId, null))
                        .addSubEntity(getCollectsSubEntity(containerId, null))
                        .addLink(getContainerSelfLink(containerId))
                        .addLink(getConfigurationsLink())
                        .addLink(getCollectZonesInRangeLink(getContainer.latitude, getContainer.longitude)),
                OK);
    }

    @GetMapping(value=GET_CONTAINERS_IN_RANGE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getContainersWithOccupationInRange(@RequestParam(MIN_RANGE_QUERY_PARAM) int minRange,
                                                             @RequestParam(MAX_RANGE_QUERY_PARAM) int maxRange)
            throws Exception {
        return new ResponseEntity(
                new SirenOutput(of(new OccupationInRangeReturn(containerRequester.getContainersWithOccupationBetweenRange(minRange, maxRange))),CONTAINERS_IN_RANGE_CLASS)
                        .addLink(getContainersInRangeSelfLink(minRange, maxRange)),
                OK
        );
    }

    @GetMapping(value=GET_ROUTE_CONTAINERS_IN_RANGE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getContainersOfARouteWithOccupationInRange(
            @PathVariable(ROUTE_ID_PATH_VAR_NAME) int routeId,
            @RequestParam(MIN_RANGE_QUERY_PARAM) int minRange,
            @RequestParam(MAX_RANGE_QUERY_PARAM) int maxRange)throws Exception {
        return new ResponseEntity(
                new SirenOutput(of(new OccupationInRangeReturn(containerRequester.getContainersOfARouteWithOccupationBetweenRange(routeId, minRange, maxRange))),
                        CONTAINERS_IN_RANGE_CLASS, ROUTE_CLASS
                )
                        .addLink(getContainersOfARouteInRangeSelfLink(routeId, minRange, maxRange))
                        .addLink(getRouteUpLink(routeId)),
                OK
        );
    }

    /**
     * Utility methods
     */

    public static void writeToFile(int containerId, ContainerReadsInput reads) {
        try {
            FileUtils.writeStringToFile(new File(String.format(READS_LOG_FILE_NAME_TEMPLATE, containerId)),
                    LocalDateTime.now().toString() + " => " + reads.toString(), true);
        } catch (IOException e) {
           log.error("Couldn't write the newest read for container with id {}. {} => {}", containerId, LocalDateTime.now().toString(), reads.toString());
        }
    }

    private static List<Item> extractContainerItems (List<Container> containers) throws URISyntaxException {
        List<Item> items = new ArrayList<>(containers.size());
        for (Container c : containers) {
            Item item = new Item(new URI(GET_CONTAINER_PATH.replace(CONTAINER_ID_PATH_VAR, "" + c.containerId)))
                    .addProperty(new Property(CONTAINER_ID_FIELD_NAME, of("" + c.containerId), of(CONTAINER_ID_TITLE), empty()))
                    .addProperty(new Property(IOT_ID_FIELD_NAME, of(c.iotId), of(IOT_ID_TITLE), empty()))
                    .addProperty(new Property(ACTIVE_FIELD_NAME, of(c.active.equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)),
                            of(ACTIVE_TITLE), empty()))
                    .addProperty(new Property(LATITUDE_FIELD_NAME, of("" + c.latitude), of(LATITUDE_TITLE), empty()))
                    .addProperty(new Property(LONGITUDE_FIELD_NAME, of("" + c.longitude), of(LONGITUDE_TITLE), empty()))
                    .addProperty(new Property(HEIGHT_FIELD_NAME, of("" + c.height), of(LATITUDE_TITLE), empty()))
                    .addProperty(new Property(CONTAINER_TYPE_FIELD_NAME, of(c.containerType), of(CONTAINER_TYPE_TITLE), empty()))
                    .addProperty(new Property(LAST_READ_DATE_FIELD_NAME, c.lastReadDate != null? of(c.lastReadDate.toString()) : empty(), of(LAST_READ_DATE_TITLE), empty()))
                    .addProperty(new Property(BATTERY_FIELD_NAME, of("" + c.battery), of(BATTERY_TITLE), empty()))
                    .addProperty(new Property(OCCUPATION_FIELD_NAME, of("" + c.occupation), of(OCCUPATION_TITLE), empty()))
                    .addProperty(new Property(TEMPERATURE_FIELD_NAME, of("" + c.temperature), of(TEMPERATURE_TITLE), empty()))
                    .addProperty(new Property(COLLECT_ZONE_ID_FIELD_NAME, of("" + c.collectZoneId), of(COLLECT_ZONE_ID_TITLE), empty()))
                    .addProperty(new Property(CONFIGURATION_ID_FIELD_NAME, of("" + c.configurationId),of(CONFIGURATION_ID_TITLE), empty()));
            items.add(item);
        }
        return items;
    }

    /**
     * Verify if the container type given in the request body is valid, before the communication with database proceed.
     * The list of containers is available in the class ContainerMapper.
     *
     * @param containerType container type given in the request body
     * @return true if the container type is valid, false if not
     */
    public static boolean isContainerTypeValid(String containerType){
        return ContainerMapper.CONTAINER_TYPES.stream()
                .filter(field -> field.value.equalsIgnoreCase(containerType))
                .findFirst()
                .isPresent();
    }

    /**
     * Used to convert an object into a byte array, since this is the only type that de IOT device
     * can read
     */
    private static String convertContainerConfigurationsIntoHexa(int height,
                                                                 List<ConfigurationCommunication> configurationCommunications){
        String configurations = String.format("%08X", height);

        configurations += configurationCommunications.stream()
                .map(conf -> String.format("%02X", conf.value))
                .reduce("", String::concat);
        return String.format("%s%s", configurations, hexaDataFiller(0, SIGFOX_RESPONSE_LENGTH - configurations.length()));
    }

    /**
     * Read ContainerReadsInput data, and fill fields temperature, occupation and battery
     *
     * It was defined, by the IOT developer, that the only data that should be consumed and treated
     * by the API is the first three bytes, following the pattern IIOOTT, where II informs if the
     * reads can have errors, OO the value for occupation and TT the value for temperature
     *
     * @return true if the reads should be stored somewhere or false if the reads should not be considered
     */
    private static boolean parseBodyData(ContainerReadsInput readsInput){
        long initializationProcessMask = 0xFF000000;
        long value = Long.decode("0x" + readsInput.data.substring(9));
        if ((value & initializationProcessMask) != 0) return false;
        long occupationMaskNoSignal = 0x7F00;
        long temperatureMaskNoSignal = 0x7F;
        long occupationMask = 0xFF00;
        long temperatureMask = 0xFF;
        long signalMask = 0x80;
        readsInput.battery = -1; //Unavailable feature

        short occupationValueWithSignal = (short)((occupationMask & value) >> 8);
        short temperatureValueWithSignal = (short)(temperatureMask & value);
        short occupationValueWithoutSignal = (short)((occupationMaskNoSignal & value) >> 8);
        short temperatureValueWithoutSignal = (short)(temperatureMaskNoSignal & value);

        readsInput.occupation = (short) ((-signalMask * ((occupationValueWithSignal & signalMask) >> 7)) + occupationValueWithoutSignal);
        readsInput.temperature = (short) ((-signalMask * ((temperatureValueWithSignal & signalMask) >> 7)) + temperatureValueWithoutSignal);
        return true;
    }

    /**
     * Fill response body with a pattern
     * @param pattern pattern to produce
     * @param bytesToFill number of times to produce the pattern
     * @return a string representing a hexadecimal number
     */
    private static String hexaDataFiller(int pattern, int bytesToFill){
        if(bytesToFill <= 0) return "";
        String data = "";
        for (int i = 0; i < bytesToFill/2; i++) {
            data += String.format("%02X", pattern);
        }
        return data;
    }

    /**
     * Templates
     */

    private static Template getContainerTemplate() {
        return new Template()
                .addProperty(new Property(IOT_ID_FIELD_NAME, empty(), of(IOT_ID_TITLE), empty()))
                .addProperty(new Property(LATITUDE_FIELD_NAME, empty(), of(LATITUDE_TITLE), empty()))
                .addProperty(new Property(LONGITUDE_FIELD_NAME, empty(), of(LONGITUDE_TITLE), empty()))
                .addProperty(new Property(HEIGHT_FIELD_NAME, empty(), of(HEIGHT_TITLE), empty()))
                .addProperty(new Property(CONTAINER_TYPE_FIELD_NAME, empty(), of(CONTAINER_TYPE_TITLE), of(ContainerMapper.CONTAINER_TYPES)))
                .addProperty(new Property(CONFIGURATION_ID_FIELD_NAME, empty(),of(CONFIGURATION_ID_TITLE), empty()));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            UPDATE_CONTAINER_CONFIGURATION_ACTION_NAME = "update-container-configuration",
            UPDATE_CONTAINER_LOCALIZATION_ACTION_NAME = "update-container-localization",
            UPDATE_CONTAINER_READS_ACTION_NAME = "update-container-reads",
            DEACTIVATE_CONTAINER_ACTION_NAME = "deactivate-container",
            ACTIVATE_CONTAINER_ACTION_NAME = "activate-container";

    private static SirenAction getUpdateContainerConfigurationAction(int containerId) throws URISyntaxException {
        return new SirenAction(UPDATE_CONTAINER_CONFIGURATION_ACTION_NAME, empty(),HttpMethod.PUT,
                        new URI(UPDATE_CONTAINER_CONFIGURATION_PATH.replace(CONTAINER_ID_PATH_VAR,"" + containerId)),
                        MediaType.APPLICATION_JSON)
                .addField(new Field(IOT_ID_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(IOT_ID_TITLE), empty()))
                .addField(new Field(HEIGHT_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(HEIGHT_TITLE), empty()))
                .addField(new Field(CONTAINER_TYPE_FIELD_NAME, empty(), of(TEXT_DATA_TYPE), empty(), of(CONTAINER_TYPE_TITLE),
                        of(ContainerMapper.CONTAINER_TYPES)))
                .addField(new Field(CONFIGURATION_ID_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(CONFIGURATION_ID_TITLE), empty()));
    }

    private static SirenAction getUpdateContainerLocalizationAction(int containerId) throws URISyntaxException {
        return new SirenAction(UPDATE_CONTAINER_LOCALIZATION_ACTION_NAME, empty(),HttpMethod.PUT,
                        new URI(UPDATE_CONTAINER_LOCALIZATION_PATH.replace(CONTAINER_ID_PATH_VAR,"" + containerId)),
                        MediaType.APPLICATION_JSON)
                .addField(new Field(COLLECT_ZONE_ID_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(COLLECT_ZONE_ID_TITLE), empty()))
                .addField(new Field(LATITUDE_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(LATITUDE_TITLE), empty()))
                .addField(new Field(LONGITUDE_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(LONGITUDE_TITLE), empty()));
    }

    private static SirenAction getUpdateContainerReadsAction(int iotID) throws URISyntaxException {
        return new SirenAction(UPDATE_CONTAINER_READS_ACTION_NAME, empty(),HttpMethod.PUT,
                        new URI(UPDATE_CONTAINER_READS_PATH.replace(IOT_ID_PATH_VAR,"" + iotID)),
                        MediaType.APPLICATION_JSON)
                .addField(new Field(BATTERY_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(BATTERY_TITLE), empty()))
                .addField(new Field(TEMPERATURE_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(TEMPERATURE_TITLE), empty()))
                .addField(new Field(OCCUPATION_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(OCCUPATION_TITLE), empty()));
    }


    private SirenAction getActivateContainerAction(int containerId) throws URISyntaxException {
        return new SirenAction(ACTIVATE_CONTAINER_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(ACTIVATE_CONTAINER_PATH.replace(CONTAINER_ID_PATH_VAR,"" + containerId)), MediaType.ALL);
    }

    private SirenAction getDeactivateContainerAction(int containerId) throws URISyntaxException {
        return new SirenAction(DEACTIVATE_CONTAINER_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(DEACTIVATE_CONTAINER_PATH.replace(CONTAINER_ID_PATH_VAR,"" + containerId)), MediaType.ALL);
    }

    /**
     * Sub Entities
     */

    private static SubEntity getConfigurationSubEntity(int configurationId, Object properties)
            throws URISyntaxException {
        return new SubEntity(
                new URI(ConfigurationController.GET_CONFIGURATION_PATH
                        .replace(ConfigurationController.CONFIGURATION_ID_PATH_VAR, "" + configurationId)),
                new String[]{CONFIGURATION_REL}, properties, empty(), empty(),
                CONFIGURATION_CLASS
        );
    }

    private static SubEntity getCollectZoneSubEntity(int collectZoneId, Object properties)
            throws URISyntaxException {
        return new SubEntity(
                null,
                new String[]{COLLECT_ZONE_REL}, properties, empty(), empty(), COLLECT_ZONE_CLASS
        )
                .addLink(new SirenLink(new URI(CollectZoneController.GET_COLLECT_ZONE_PATH
                        .replace(CollectZoneController.COLLECT_ZONE_ID_PATH_VAR, "" + collectZoneId)),
                        SirenLink.SELF_REL));
    }

    private static SubEntity getCollectsSubEntity(int containerId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(CollectController.GET_CONTAINER_COLLECTS_PATH.replace(CollectController.CONTAINER_ID_PATH_VAR, "" + containerId)),
                new String[]{COLLECT_REL}, properties, empty(), empty(), COLLECT_CLASS, COLLECTION_CLASS
        );
    }

    private static SubEntity getWashesSubEntity(int containerId, Object properties) throws URISyntaxException {
        return new SubEntity(
                new URI(WashController.GET_CONTAINER_WASHES_PATH.replace(WashController.CONTAINER_ID_PATH_VAR, "" + containerId)),
                new String[]{WASH_REL}, properties, empty(), empty(), WASH_CLASS, COLLECTION_CLASS
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getContainerSelfLink(int containerId) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_CONTAINER_PATH.replace(CONTAINER_ID_PATH_VAR, "" + containerId)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getConfigurationsLink() throws URISyntaxException {
        return new SirenLink(
                new URI(ConfigurationController.GET_ALL_CONFIGURATIONS_PATH),
                CONFIGURATION_LIST_REL
        );
    }

    private static SirenLink getCollectZonesInRangeLink(float latitude, float longitude) throws URISyntaxException {
        return new SirenLink(
                new URI(CollectZoneController.GET_COLLECT_ZONES_IN_RANGE_PATH + "?" + CollectZoneController.LATITUDE_QUERY_PARAM + "=" + latitude + "&" +
                        CollectZoneController.LONGITUDE_QUERY_PARAM + "=" + longitude),
                COLLECT_ZONES_IN_RANGE_REL
        );
    }

    private static SirenLink getContainersInRangeSelfLink(int min, int max) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_CONTAINERS_IN_RANGE_PATH + "?" + MIN_RANGE_QUERY_PARAM + "=" + min + "&" + MAX_RANGE_QUERY_PARAM + "=" + max),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getContainersOfARouteInRangeSelfLink(int routeId, int min, int max) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_ROUTE_CONTAINERS_IN_RANGE_PATH.replace(ROUTE_ID_PATH_VAR, "" + routeId)
                        + "?" + MIN_RANGE_QUERY_PARAM + "=" + min + "&" + MAX_RANGE_QUERY_PARAM + "=" + max),
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
