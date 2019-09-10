package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Configuration;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface ConfigurationRequester {

    /**
     * Creates a new configuration with the given name
     * @param configurationName name of the new configuration
     * @return identifier of the new configuration
     */
    int createConfiguration (String configurationName) throws Exception;

    /**
     * Updates the configuration.
     * @param configurationId identifier of the configuration
     * @param configurationName the new name to associate to the configuration
     */
    void updateConfiguration (int configurationId, String configurationName) throws Exception;

    /**
     * Deletes a configuration
     * @param configurationId identifier of the configuration to be deleted
     */
    void deleteConfiguration (int configurationId) throws Exception;

    /**
     * Returns a list with all the existent configurations
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements with information about the configurations
     */
    PaginatedList<Configuration> getAllConfigurations (int pageNumber, int rowsPerPage) throws Exception;

    /**
     * Get configuration info
     * @param configurationid identifier of the configuration to search
     * @return an instance of the object Configuration
     */
    Configuration getConfiguration (int configurationid) throws Exception;
}
