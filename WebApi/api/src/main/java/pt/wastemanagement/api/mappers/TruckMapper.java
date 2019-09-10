package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.Truck;
import pt.wastemanagement.api.requesters.TruckRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class TruckMapper implements TruckRequester {
    public static final String
            REGISTRATION_PLATE_COLUMN_NAME = "registration_plate",
            ACTIVE_COLUMN_NAME = "active",
    //Generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_TRUCK = "{call dbo.CreateTruck(?)}", //truck_plate
            DEACTIVATE_TRUCK = "{call dbo.DeactivateTruck(?)}", //truck_plate
            ACTIVATE_TRUCK = "{call dbo.ActivateTruck(?)}", //truck_plate
            GET_ALL_TRUCKS = "SELECT * FROM dbo.GetAllTrucks(?,?)",//page, rows
            GET_ALL_ACTIVE_TRUCKS = "SELECT * FROM dbo.GetAllTrucks(?,?)", //page, rows
            GET_TRUCK = "SELECT * FROM dbo.GetTruck(?)"; //truck_plate


    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(TruckMapper.class);

    public TruckMapper (Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new truck
     * @param truckPlate registration plate of the new truck
     * @throws SQLException
     */
    public void createTruck (String truckPlate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_TRUCK)) {
            st.setString(1, truckPlate);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @TruckMapper.createTruck()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Activates a truck. If its already active, will not be produced any side effects.
     * @param truckPlate registration plate of the truck
     * @throws SQLNonExistentEntryException if the entry with the identified by (@truckPlate)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void activateTruck (String truckPlate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(ACTIVATE_TRUCK)) {
            st.setString(1, truckPlate);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @TruckMapper.activateTruck()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to activate a non existent entry on Truck     table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deactivates a truck. If its already inactive, will not be produced any side effects.
     * @param truckPlate registration plate of the truck
     * @throws SQLNonExistentEntryException if the entry with the identified by (@truckPlate)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deactivateTruck (String truckPlate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DEACTIVATE_TRUCK)) {
            st.setString(1, truckPlate);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @TruckMapper.deactivateTruck()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to deactivate a non existent entry on Truck table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Gets all the trucks registered on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param showInactive if true, all active and inactive entries will be showed, case false, only active
     * @return a list with a maximum of @rowsPerPage elements that represents the trucks
     * @throws SQLException
     */
    public PaginatedList<Truck> getAllTrucks (int pageNumber, int rowsPerPage, boolean showInactive) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Truck> trucks = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(showInactive ? GET_ALL_TRUCKS : GET_ALL_ACTIVE_TRUCKS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, trucks);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                trucks.add(new Truck(rs.getString(REGISTRATION_PLATE_COLUMN_NAME),
                        rs.getString(ACTIVE_COLUMN_NAME).equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, trucks);
        } catch (SQLException e) {
            log.warn("Error on @TruckMapper.getAllTrucks()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @TruckMapper.getAllTrucks()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @TruckMapper.getAllTrucks() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get a container wash info
     * @param truckPlate identifier of the truck
     * @return an instance of Wash
     * @throws SQLException
     */
    public Truck getTruck (String truckPlate) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_TRUCK)) {
            st.setString(1, truckPlate);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent truck");
                return null;
            }
            return new Truck(rs.getString(REGISTRATION_PLATE_COLUMN_NAME),
                    rs.getString(ACTIVE_COLUMN_NAME).equalsIgnoreCase("T") ? Boolean.toString(true) : Boolean.toString(false));
        } catch (SQLException e) {
            log.warn("Error on @TruckMapper.getTruck()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @TruckMapper.getTruck()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @TruckMapper.getTruck() " +
                        "because it was null");
            }
        }
    }
}
