package pt.wastemanagement.api.requesters;

import org.springframework.stereotype.Component;
import pt.wastemanagement.api.model.Communication;
import pt.wastemanagement.api.model.utils.PaginatedList;

import java.sql.*;

public interface CommunicationRequester {

    /**
     * Creates a new communication
     * @param communicationDesignation designation of the new communication
     * @return the identifier of the created communication
     */
    int createCommunication (String communicationDesignation) throws Exception;

    /**
     * Updates the communication.
     * @param communicationId identifier of the communication
     * @param communicationDesignation the new name to associate to the communication
     */
    void updateCommunication (int communicationId, String communicationDesignation) throws Exception;

    /**
     * Deletes the communication.
     * @param communicationId identifier of the communication to deleted
     */
    void deleteCommunication (int communicationId) throws Exception;

    /**
     * Get all existent communications
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents the communications
     */
    PaginatedList<Communication> getAllCommunications(int pageNumber, int rowsPerPage) throws Exception;

    /**
     * Get communication info
     * @param communicationId identifier of the communication to search
     * @return an instance of ContainerStatistics
     * @throws SQLException
     */
    Communication getCommunication(int communicationId) throws Exception;


}
