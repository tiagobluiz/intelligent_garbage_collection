package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.functions.CollectZoneStatistics;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocation;
import pt.wastemanagement.api.model.functions.CollectZoneWithLocationAndOccupationInfo;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectZoneRequester;

import java.util.ArrayList;
import java.util.List;

public class CollectZoneRequesterImplementation implements CollectZoneRequester {
    public final static int
            NORMAL_STATE = 0,                 // Normal usage of the requester
            WRONG_PARAMETERS_STATE = 1,       // Wrong parameters
            BAD_REQUEST_STATE = 2;            // Unpredictable usage of the requester

    public static final int CONTAINER_ID = 1;
    public static final int TOTAL_COLLECT_ZONES = 1, COLLECT_ZONE_ID = 1, PICK_ORDER = 1, ROUTE_ID = 1;
    public static final float LATITUDE = 0, LONGITUDE = 0;
    public static final String ACTIVE = "T";
    public static final short
            GENERAL_OCCUPATION = 50, PLASTIC_OCCUPATION = 20, GLASS_OCCUPATION = 0, PAPER_OCCUPATION = 23;

    public final int implementation_state;

    public CollectZoneRequesterImplementation(int implementation_state) {
        this.implementation_state = implementation_state;
    }

    @Override
    public int createCollectZone(int routeId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLInvalidDependencyException(); // Route was invalid
        }
        return CONTAINER_ID;
    }

    @Override
    public void updateCollectZone(int collectZoneId, int routeId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE){
            throw new SQLInvalidDependencyException(); // Route is invalid
        } else if (implementation_state == BAD_REQUEST_STATE){
            throw new SQLNonExistentEntryException(); //Collect zone doesn't exists
        }
        return;
    }

    @Override
    public void deactivateCollectZone(int collectZoneId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Collect zone didn't exists
        }
        return;
    }

    @Override
    public void activateCollectZone(int collectZoneId) throws Exception {
        if (implementation_state == WRONG_PARAMETERS_STATE || implementation_state == BAD_REQUEST_STATE) {
            throw new SQLNonExistentEntryException(); // Collect zone didn't exists
        }
        return;
    }

    @Override
    public PaginatedList<CollectZoneWithLocation> getRouteCollectZones(int pageNumber, int rowsPerPage, int routeId, boolean showInactive) throws Exception {
        List<CollectZoneWithLocation> collectZones = new ArrayList<>();
        collectZones.add(new CollectZoneWithLocation(COLLECT_ZONE_ID, routeId, PICK_ORDER, ACTIVE, LATITUDE, LONGITUDE));

        return new PaginatedList<>(TOTAL_COLLECT_ZONES, collectZones);
    }

    @Override
    public CollectZoneWithLocationAndOccupationInfo getCollectZoneInfo(int collectZoneId) throws Exception {
        return new CollectZoneWithLocationAndOccupationInfo(collectZoneId, ROUTE_ID, PICK_ORDER, ACTIVE, LATITUDE, LONGITUDE,
                GENERAL_OCCUPATION, PLASTIC_OCCUPATION, PAPER_OCCUPATION, GLASS_OCCUPATION);
    }

    @Override
    public CollectZoneStatistics getCollectZoneStatistics(int collectZoneId) throws Exception {
        return new CollectZoneStatistics(collectZoneId, TOTAL_COLLECT_ZONES);
    }

    @Override
    public PaginatedList<CollectZoneWithLocation> getRouteCollectionPlan(int pageNumber, int rowsPerPage, int routeId, String containerType) throws Exception {
        List<CollectZoneWithLocation> collectZones = new ArrayList<>();
        collectZones.add(new CollectZoneWithLocation(COLLECT_ZONE_ID, routeId, PICK_ORDER, ACTIVE, LATITUDE, LONGITUDE));

        return new PaginatedList<>(TOTAL_COLLECT_ZONES, collectZones);
    }

    @Override
    public List<CollectZoneWithLocation> getCollectZonesInRange(float latitude, float longitude, int range) throws Exception {
        List<CollectZoneWithLocation> collectZones = new ArrayList<>();
        collectZones.add(new CollectZoneWithLocation(COLLECT_ZONE_ID, ROUTE_ID, PICK_ORDER, ACTIVE, LATITUDE, LONGITUDE));

        return collectZones;
    }
}

