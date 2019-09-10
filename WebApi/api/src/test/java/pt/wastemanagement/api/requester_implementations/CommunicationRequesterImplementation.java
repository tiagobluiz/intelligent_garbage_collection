package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLDependencyBreakException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Communication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CommunicationRequester;

import java.util.ArrayList;
import java.util.List;

public class CommunicationRequesterImplementation implements CommunicationRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public CommunicationRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    @Override
    public int createCommunication(String communicationDesignation) throws Exception {
        return 1;
    }

    @Override
    public void updateCommunication(int communicationId, String communicationDesignation) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Communication didn't exists
        }
        return;
    }

    @Override
    public void deleteCommunication(int communicationId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLDependencyBreakException(); //Communication its not available to be deleted
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); //Communication Id is invalid
        }
        return;
    }

    public static final int TOTAL_COMMUNICATIONS = 1, COMMUNICATION_ID = 1;
    public static final String COMMUNICATION_NAME = "Com";
    @Override
    public PaginatedList<Communication> getAllCommunications(int pageNumber, int rowsPerPage) throws Exception {
        List<Communication> communications = new ArrayList<>();
        communications.add(new Communication(COMMUNICATION_ID, COMMUNICATION_NAME));

        return new PaginatedList<>(TOTAL_COMMUNICATIONS, communications);
    }

    @Override
    public Communication getCommunication(int communicationId) throws Exception {
        return new Communication(COMMUNICATION_ID, COMMUNICATION_NAME);
    }
}
