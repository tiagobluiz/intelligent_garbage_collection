package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Truck;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface TruckRequester {

    /**
     * Creates a new truck
     * @param truckPlate registration plate of the new truck
     */
    void createTruck (String truckPlate) throws Exception;

    /**
     * Activates a truck.
     * @param truckPlate registration plate of the truck
     */
    void activateTruck (String truckPlate) throws Exception;

    /**
     * Deactivates a truck. If its already inactive, will not be produced any side effects.
     * @param truckPlate registration plate of the truck
     */
    void deactivateTruck (String truckPlate) throws Exception;

    /**
     * Gets all the trucks registered on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the trucks
     */
    PaginatedList<Truck> getAllTrucks (int pageNumber, int rowsPerPage, boolean showInactive) throws Exception;

    /**
     * Get a container wash info
     * @param truckPlate identifier of the truck
     * @return an instance of Wash
     */
    Truck getTruck (String truckPlate) throws Exception;
}
