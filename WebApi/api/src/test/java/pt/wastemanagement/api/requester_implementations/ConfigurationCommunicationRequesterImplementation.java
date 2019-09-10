package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationCommunicationRequester;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationCommunicationRequesterImplementation implements ConfigurationCommunicationRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public ConfigurationCommunicationRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }


    @Override
    public void associateCommunicationToTheConfiguration(int configurationId, int communicationId, int value) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Configuration or Communication were invalid
        }
        return;
    }

    @Override
    public void disassociateCommunicationToTheConfiguration(int configurationId, int communicationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // The pair identified by the key (communication, configuration) didn't exists
        }
        return;
    }

    public static final int TOTAL_CONFIGURATION_COMMUNICATIONS = 1, COMMUNICATION_ID = 1, VALUE = 70;
    public static final String COMM_NAME = "CommTest";
    @Override
    public PaginatedList<ConfigurationCommunication> getConfigurationCommunicationsList(int pageNumber, int rowsPerPage, int configurationId) throws Exception {
        List<ConfigurationCommunication> configurationCommunications = new ArrayList<>();
        configurationCommunications.add(new ConfigurationCommunication(configurationId, COMMUNICATION_ID, COMM_NAME, VALUE));

        return new PaginatedList<>(TOTAL_CONFIGURATION_COMMUNICATIONS, configurationCommunications);
    }

    @Override
    public ConfigurationCommunication getConfigurationCommunication(int configurationId, int communicationId) throws Exception {
        return new ConfigurationCommunication(configurationId, communicationId, COMM_NAME, VALUE);
    }

    @Override
    public ConfigurationCommunication getConfigurationCommunicationByName(int configurationId, String communicationName) throws Exception {
        return new ConfigurationCommunication(configurationId, COMMUNICATION_ID, VALUE);
    }
}
