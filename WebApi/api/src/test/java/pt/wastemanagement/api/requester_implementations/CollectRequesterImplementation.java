package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.exceptions.SQLWrongDateException;
import pt.wastemanagement.api.model.Collect;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectRequester;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CollectRequesterImplementation implements CollectRequester {
    public final static int
            NORMAL_STATE = 0,               // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,    // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public CollectRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }


    @Override
    public void createCollect(int containerId, LocalDateTime collectDate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE) {
            throw new SQLWrongDateException(); // //SQL couldn't interpret the date format
        } else if (implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); //E.g: Container Id is invalid
        }
        return;
    }

    @Override
    public void collectCollectZoneContainers(int collectZoneId, LocalDateTime collectDate, String containerType) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Collect Zone didn't exists
        }
        return;
    }

    @Override
    public void updateCollect(int containerId, LocalDateTime actualCollectDate, LocalDateTime newCollectDate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE) {
            throw new SQLWrongDateException(); //SQL couldn't interpret the date format
        } else if (implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); //Container Id is invalid
        }
        return;
    }

    public static final int TOTAL_COLLECTS = 1;
    public static final String
            DATE_1 = "2018-05-05T20:56:57",
            CONFIRMED = "T";

    @Override
    public PaginatedList<Collect> getContainerCollects(int pageNumber, int rowsPerPage, int containerId) throws Exception {
        List<Collect> collects = new ArrayList<>(TOTAL_COLLECTS);
        collects.add(new Collect(containerId, LocalDateTime.parse(DATE_1), CONFIRMED));
        return new PaginatedList<>(TOTAL_COLLECTS, collects);
    }


    @Override
    public Collect getContainerCollect(int containerId, LocalDateTime collectDate) throws Exception {
        return new Collect(containerId, collectDate, CONFIRMED);
    }
}
