package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Wash;
import pt.wastemanagement.api.model.utils.PaginatedList;
import java.time.LocalDateTime;

public interface WashRequester {

    /**
     * Creates a wash
     * @param containerId identifier of the container where this wash belongs
     * @param washDate date and hour when the wash was made
     */
    void createWash (int containerId, LocalDateTime washDate) throws Exception;

    /**
     * Creates a wash for every container contained in a collect zone for a given type
     * @param collectZoneId identifier of the collect zone that is being washed
     * @param washDate date and hour when the wash was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ssTZD
     * @param containerType type of the containers to be washed
     * @throws Exception
     */
    void washCollectZoneContainers (int collectZoneId, LocalDateTime washDate, String containerType) throws Exception;

    /**
     * Changes the date of a wash.
     * @param containerId identifier of the container where this wash belongs
     * @param actualWashDate the date that is stored before the update on the database
     * @param newWashDate the new date to store on database
     */
    void updateWash (int containerId, LocalDateTime actualWashDate, LocalDateTime newWashDate) throws Exception;

    /**
     * Returns a list with the washes of the container
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerId the identifier of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the washes made on the desired container
     */
    PaginatedList<Wash> getContainerWashes(int pageNumber, int rowsPerPage, int containerId) throws Exception;

    /**
     * Get a container wash info
     * @param containerId identifier of the container
     * @param washDate date and time of the wash
     * @return an instance of Wash
     */
    Wash getContainerWash (int containerId, LocalDateTime washDate) throws Exception;
}
