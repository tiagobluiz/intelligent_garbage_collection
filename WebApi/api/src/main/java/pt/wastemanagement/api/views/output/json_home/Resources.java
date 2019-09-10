package pt.wastemanagement.api.views.output.json_home;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import pt.wastemanagement.api.controllers.*;
import pt.wastemanagement.api.views.input.CollectRouteInput;

import static pt.wastemanagement.api.controllers.Controller.*;


public class Resources{
    @JsonProperty(ROUTE_LIST_REL)
    public Resource getRoutesList = new Resource(RouteController.GET_ALL_ROUTES_PATH);

    @JsonProperty(STATION_LIST_REL)
    public Resource getStationsList = new Resource(StationController.GET_ALL_STATIONS_PATH);

    @JsonProperty(CONFIGURATION_LIST_REL)
    public Resource getConfigurationsList = new Resource(ConfigurationController.GET_ALL_CONFIGURATIONS_PATH);

    @JsonProperty(COMMUNICATION_LIST_REL)
    public Resource getCommunicationsList = new Resource(CommunicationController.GET_ALL_COMMUNICATIONS_PATH);

    @JsonProperty(TRUCK_LIST_REL)
    public Resource getTrucksList = new Resource(TruckController.GET_ALL_TRUCKS_PATH);

    @JsonProperty(CONTAINERS_IN_RANGE_REL)
    public Resource getContainersOccupationBetweenrange =
            new Resource(ContainerController.GET_CONTAINERS_IN_RANGE_PATH_WITH_QUERY_PARAMS, null, null);

    @JsonProperty(EMPLOYEE_REL)
    public Resource getCurrentEmployeeInfo = new Resource(EmployeeController.GET_CURRENT_EMPLOYEE_PATH);

    @JsonProperty(COLLECT_ROUTE_REL)
    public Resource postCollectRoute = new Resource(RouteCollectionController.COLLECT_ROUTE_PATH, null,
            new Hints(new String[]{"POST"}, new Formats(new CollectRouteInput()),new String[]{MediaType.APPLICATION_JSON_VALUE}));

    @JsonProperty(COLLECT_ZONES_IN_RANGE_REL)
    public Resource collectZonesNearby = new Resource(CollectZoneController.GET_COLLECT_ZONES_IN_RANGE_PATH_WITH_QUERY_PARAMS);
}
