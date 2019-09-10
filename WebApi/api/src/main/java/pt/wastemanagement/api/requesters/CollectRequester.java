package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Collect;
import pt.wastemanagement.api.model.utils.PaginatedList;

import java.time.LocalDateTime;

public interface CollectRequester {

    /**
     * Creates a collect
     * @param containerId identifier of the container where this collect belongs
     * @param collectDate date and hour when the collect was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ssTZD
     */
    void createCollect (int containerId, LocalDateTime collectDate) throws Exception;

    /**
     * Creates a collect for every container contained in a collect zone for a given type
     * @param collectZoneId identifier of the collect zone that is being collected
     * @param collectDate date and hour when the collect was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ssTZD
     * @param containerType type of the containers to be collected
     * @throws Exception
     */
    void collectCollectZoneContainers (int collectZoneId, LocalDateTime collectDate, String containerType) throws Exception;

    /**
     * Changes the date of a collect.
     * @param containerId identifier of the container where this collect belongs
     * @param actualCollectDate the date that is stored before the update on the database
     * @param newCollectDate the new date to store on database
     */
    void updateCollect (int containerId, LocalDateTime actualCollectDate, LocalDateTime newCollectDate) throws Exception;

    /**
     * Returns a list with the collects of the container
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerId the identifier of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the collects made on the desired container
     */
    PaginatedList<Collect> getContainerCollects(int pageNumber, int rowsPerPage, int containerId) throws Exception;

    /**
     * Get a container collect info
     * @param containerId identifier of the container
     * @param collectDate date and time of the collect
     * @return an instance of Collect
     */
    Collect getContainerCollect (int containerId, LocalDateTime collectDate) throws Exception;
}
