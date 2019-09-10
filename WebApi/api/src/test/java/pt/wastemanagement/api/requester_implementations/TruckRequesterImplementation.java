package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Truck;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.TruckRequester;

import java.util.ArrayList;
import java.util.List;

public class TruckRequesterImplementation implements TruckRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public final int implementation_state;

    public TruckRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }


    @Override
    public void createTruck(String truckPlate) throws Exception {
        return;
    }

    @Override
    public void activateTruck(String truckPlate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Truck didn't exists
        }
        return;
    }

    @Override
    public void deactivateTruck(String truckPlate) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Truck didn't exists
        }
        return;
    }

    public static final int TOTAL_TRUCKS = 1;
    public static final String TRUCK_PLATE = "AB-CD-EF", ACTIVE = "T";

    @Override
    public PaginatedList<Truck> getAllTrucks(int pageNumber, int rowsPerPage, boolean showInactive) throws Exception {
        List<Truck> trucks = new ArrayList<>();
        trucks.add(new Truck(TRUCK_PLATE,ACTIVE));
        return new PaginatedList<>(TOTAL_TRUCKS, trucks);
    }

    @Override
    public Truck getTruck(String truckPlate) throws Exception {
        return new Truck(truckPlate,ACTIVE);
    }
}
