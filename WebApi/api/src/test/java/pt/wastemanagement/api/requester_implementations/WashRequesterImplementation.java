package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Wash;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.WashRequester;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WashRequesterImplementation implements WashRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public WashRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    @Override
    public void createWash(int containerId, LocalDateTime washDate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Container didn't exists
        }
        return;
    }

    @Override
    public void washCollectZoneContainers(int collectZoneId, LocalDateTime washDate, String containerType) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Collect Zone didn't exists
        }
        return;
    }

    @Override
    public void updateWash(int containerId, LocalDateTime actualWashDate, LocalDateTime newWashDate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Wash didn't exists
        }
        return;
    }

    public static final int TOTAL_WASHES = 1;
    public static final String WASH_DATE = "2018-07-14T23:59:59";
    @Override
    public PaginatedList<Wash> getContainerWashes(int pageNumber, int rowsPerPage, int containerId) throws Exception {
        List<Wash> washes = new ArrayList<>();
        washes.add(new Wash(containerId, LocalDateTime.parse(WASH_DATE)));
        return new PaginatedList<>(TOTAL_WASHES, washes);
    }

    @Override
    public Wash getContainerWash(int containerId, LocalDateTime washDate) throws Exception {
        return new Wash(containerId, washDate);
    }
}