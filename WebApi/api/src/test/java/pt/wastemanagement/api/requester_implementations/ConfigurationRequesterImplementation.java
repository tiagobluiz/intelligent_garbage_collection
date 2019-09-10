package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Configuration;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationRequester;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationRequesterImplementation implements ConfigurationRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public ConfigurationRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }


    @Override
    public int createConfiguration(String configurationName) throws Exception {
        return 1;
    }

    @Override
    public void updateConfiguration(int configurationId, String configurationName) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Configuration didn't exists
        }
        return;
    }

    @Override
    public void deleteConfiguration(int configurationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Configuration didn't exists
        }
        return;
    }

    public static final int TOTAL_CONFIGURATIONS = 1, CONFIGURATION_ID = 1;
    public static final String CONFIGURATION_NAME = "Config";
    @Override
    public PaginatedList<Configuration> getAllConfigurations(int pageNumber, int rowsPerPage) throws Exception {
        List<Configuration> configurations = new ArrayList<>();
        configurations.add(new Configuration(CONFIGURATION_ID, CONFIGURATION_NAME));

        return new PaginatedList<>(TOTAL_CONFIGURATIONS, configurations);
    }

    @Override
    public Configuration getConfiguration(int configurationid) throws Exception {
        return new Configuration(CONFIGURATION_ID, CONFIGURATION_NAME);
    }
}