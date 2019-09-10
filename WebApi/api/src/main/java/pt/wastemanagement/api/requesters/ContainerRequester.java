package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Container;
import pt.wastemanagement.api.model.functions.ContainerStatistics;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface ContainerRequester {

    /**
     * Creates a new container
     * @param iotId identifier of the embedded system
     * @param latitude latitude coordinates of the container final location
     * @param longitude longitude coordinates of the container final location
     * @param height height of the container
     * @param containerType type of the container
     * @param collectZoneId identifier of the collect zone where this container will, initially, be inserted
     * @param configurationId identifier of the configuration to associate to the container
     * @return the identifier of the new container
     */
    int createContainer (String iotId, float latitude, float longitude, int height, String containerType,
                                int collectZoneId, int configurationId) throws Exception;

    /**
     * Updates the information of a container.
     * @param containerId identifier of the container to be updated
     * @param iotId new identifier of the embedded system
     * @param height new height of the container
     * @param containerType type of the container
     * @param configurationId new identifier of the configuration to associate to the container
     */
    void updateContainerConfiguration (int containerId, String iotId, int height, String containerType, int configurationId)
            throws Exception;

    /**
     * Updates the localization of the container.
     * @param containerId identifier of the container to update
     * @param latitude new latitude coordinates of the container final location
     * @param longitude new longitude coordinates of the container final location
     * @param collectZoneId new identifier of the collect zone where this container will be inserted. If a new
     *                      collect zone should be created, then the value MUST be -1, and it will be inserted in
     *                      the route that the current collect zone is. If the container will be relocated to an
     *                      existent collect zone that has already one or more containers, then the localization
                            MUST be at a maximum of 10 meters of distance of the other containers.
     */
    void updateContainerLocalization (int containerId, float latitude, float longitude, int collectZoneId)
            throws Exception;

    /**
     * Updates container reads.
     * @param iotId identifier of the container to be updated
     * @param battery new value of the battery
     * @param occupation new value of the occupation
     * @param temperature new value of the temperature
     */
    void updateContainerReads (String iotId, short battery, short occupation, short temperature) throws Exception;

    /**
     * Deactivates a container
     * @param containerId identifier of the container to be deactivated
     */
    void deactivateContainer (int containerId) throws Exception;

    /**
     * Activates a container
     * @param containerId identifier of the container to be deactivated
     */
    void activateContainer (int containerId) throws Exception;

    /**
     * Returns the containers of a collect zone
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param collectZoneId identifier of the collect zone to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements representing all the containers on the respective collect zone
     */
    PaginatedList<Container> getCollectZoneContainers(int pageNumber, int rowsPerPage, int collectZoneId,
                                                                            boolean showInactive) throws Exception;

    /**
     * Returns all the containers of a route
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param routeId identifier of the route to search
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements representing all the containers of a route
     */
    PaginatedList<Container> getRouteContainers (int pageNumber, int rowsPerPage, int routeId, boolean showInactive) throws Exception;

    /**
     * Returns all the data available for a specific container
     * @param containerId identifier of the container
     * @return an instance of GetContainerInfo
     */
    Container getContainerInfo(int containerId) throws Exception;

    /**
     * Get statistics of a single container. This includes information about the number of collects and washes.
     * @param containerId identifier of the container to search
     * @return an instance of ContainerStatistics
     */
    ContainerStatistics getContainerStatistics(int containerId) throws Exception;

    /**
     * Returns all the data available for a specific container identified by an iot id
     * @param iotId identifier of the container
     * @return an instance of GetContainerInfo
     */
    Container getContainerByIotId(String iotId) throws Exception;

    /**
     * Returns the percentage of the containers in the whole system which occupation value is on the
     * desired range
     * @param min minimum value of the range, inclusive
     * @param max maximum value of the range, inclusive
     * @return an int between 0 and 100 that represent the percentage of containers
     * which the occupation is on the given range
     */
    float getContainersWithOccupationBetweenRange(int min, int max) throws Exception;

    /**
     * Returns the percentage of the containers in one route which occupation value is on the
     * desired range
     * @param min minimum value of the range, inclusive
     * @param max maximum value of the range, inclusive
     * @return an int between 0 and 100 that represent the percentage of containers
     * which the occupation is on the given range
     */
    float getContainersOfARouteWithOccupationBetweenRange(int routeId, int min, int max) throws Exception;
}
