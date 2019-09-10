package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface ConfigurationCommunicationRequester {

    /**
     * Associates a communication to a configuration
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     */
    void associateCommunicationToTheConfiguration (int configurationId, int communicationId, int value) throws Exception;

    /**
     * Disassociates a communication to a configuration
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     */
    void disassociateCommunicationToTheConfiguration (int configurationId, int communicationId) throws Exception;

    /**
     * Returns the communications associated to a configuration
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param configurationId identifier of the configuration
     * @return a list with a maximum of @rowsPerPage elements that represents the communications of a configuration
     */
    PaginatedList<ConfigurationCommunication> getConfigurationCommunicationsList(int pageNumber, int rowsPerPage,
                                                                                        int configurationId) throws Exception;

    /**
     * Gets a specific configuration communication info
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     * @return an instance of ConfigurationCommunication
     */
    ConfigurationCommunication getConfigurationCommunication (int configurationId, int communicationId) throws Exception;

    /**
     * Gets a specific configuration communication info, having the communication name
     * @param configurationId identifier of the configuration
     * @param communicationName name of the communication
     * @return an instance of ConfigurationCommunication
     */
    ConfigurationCommunication getConfigurationCommunicationByName (int configurationId, String communicationName) throws Exception;
}
