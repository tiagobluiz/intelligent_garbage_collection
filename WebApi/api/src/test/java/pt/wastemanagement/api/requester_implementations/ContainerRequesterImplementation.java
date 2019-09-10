package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Container;
import pt.wastemanagement.api.model.functions.ContainerStatistics;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ContainerRequester;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ContainerRequesterImplementation implements ContainerRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public ContainerRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    public static final int CONTAINER_ID = 1, CONFIGURATION_ID = 1, COLLECT_ZONE_ID = 1, NUM_WASHES = 500, NUM_COLLECTS = 1000;
    public static final float LATITUDE = 0, LONGITUDE = 0, CONTAINERS_IN_RANGE_RETURN = 50;
    public static final String CONTAINER_TYPE = "general", IOT_ID = "1", ACTIVE = "T";
    public static final short HEIGHT = 250, BATTERY = 50, OCCUPATION = 50, TEMPERATURE = 30;
    public static final String LAST_READ_DATE = "2018-05-28T17:17";
    @Override
    public int createContainer(String iotId, float latitude, float longitude, int height, String containerType, int collectZoneId, int configurationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Configuration or Collect Zone were invalid
        }
        return CONTAINER_ID;
    }

    @Override
    public void updateContainerConfiguration(int containerId, String iotId, int height, String containerType, int configurationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); // Configuration is invalid
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); //Container doesn't exists
        }
        return;
    }

    @Override
    public void updateContainerLocalization(int containerId, float latitude, float longitude, int collectZoneId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); // Collect Zone is invalid
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); //Container doesn't exists
        }
        return;
    }

    @Override
    public void updateContainerReads(String iotId, short battery, short occupation, short temperature) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Container didn't exists
        }
        return;
    }

    @Override
    public void deactivateContainer(int containerId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Container didn't exists
        }
        return;
    }

    @Override
    public void activateContainer(int containerId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Container didn't exists
        }
        return;
    }

    public static final int TOTAL_CONTAINERS = 1;
    @Override
    public PaginatedList<Container> getCollectZoneContainers(int pageNumber, int rowsPerPage, int collectZoneId, boolean showInactive) throws Exception {
        List<Container> containers = new ArrayList<>();
        containers.add(new Container(CONTAINER_ID, IOT_ID, ACTIVE, LATITUDE, LONGITUDE, HEIGHT, CONTAINER_TYPE, LocalDateTime.parse(LAST_READ_DATE), TEMPERATURE,
                OCCUPATION, collectZoneId, CONFIGURATION_ID, BATTERY
        ));

        return new PaginatedList<>(TOTAL_CONTAINERS, containers);
    }

    @Override
    public PaginatedList<Container> getRouteContainers(int pageNumber, int rowsPerPage, int routeId, boolean showInactive) throws Exception {
        List<Container> containers = new ArrayList<>();
        containers.add(new Container(CONTAINER_ID, IOT_ID, ACTIVE, LATITUDE, LONGITUDE, HEIGHT, CONTAINER_TYPE, LocalDateTime.parse(LAST_READ_DATE), TEMPERATURE,
                OCCUPATION, COLLECT_ZONE_ID, CONFIGURATION_ID, BATTERY
        ));

        return new PaginatedList<>(TOTAL_CONTAINERS, containers);
    }

    @Override
    public Container getContainerInfo(int containerId) throws Exception {
        return new Container(CONTAINER_ID, IOT_ID, ACTIVE, LATITUDE, LONGITUDE, HEIGHT, CONTAINER_TYPE, LocalDateTime.parse(LAST_READ_DATE), TEMPERATURE,
                OCCUPATION, COLLECT_ZONE_ID, CONFIGURATION_ID, BATTERY
        );
    }

    @Override
    public Container getContainerByIotId(String iotId) throws Exception {
        return new Container(CONTAINER_ID, IOT_ID, ACTIVE, LATITUDE, LONGITUDE, HEIGHT, CONTAINER_TYPE, LocalDateTime.parse(LAST_READ_DATE), TEMPERATURE,
                OCCUPATION, COLLECT_ZONE_ID, CONFIGURATION_ID, BATTERY
        );
    }

    @Override
    public ContainerStatistics getContainerStatistics(int containerId) throws Exception {
        return new ContainerStatistics(containerId, NUM_WASHES, NUM_COLLECTS);
    }

    @Override
    public float getContainersWithOccupationBetweenRange(int min, int max) throws Exception {
        return CONTAINERS_IN_RANGE_RETURN;
    }

    @Override
    public float getContainersOfARouteWithOccupationBetweenRange(int routeId, int min, int max) throws Exception {
        return CONTAINERS_IN_RANGE_RETURN;
    }
}