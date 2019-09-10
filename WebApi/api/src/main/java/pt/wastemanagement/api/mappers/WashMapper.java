package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.model.Wash;
import pt.wastemanagement.api.requesters.WashRequester;

import javax.inject.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class WashMapper implements WashRequester {
    public static final String
            CONTAINER_ID_COLUMN_NAME = "container_id",
            WASH_DATE_COLUMN_NAME = "wash_date",
    //generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_WASH = "{call dbo.CreateWash(?,?)}", //container_id, wash_date
            WASH_COLLECT_ZONE_CONTAINERS = "{call dbo.WashCollectZoneContainers(?,?,?)}", //COLLECT_ZONE_ID, wash_date, CONTAINER_TYPE
            UPDATE_WASH = "{call dbo.UpdateWash(?,?,?)}", //container_id, old_wash_date, new_wash_date
            GET_CONTAINER_WASHES = "SELECT * FROM dbo.GetContainerWashes(?,?,?)", //page, rows, container_id
            GET_CONTAINER_WASH = "SELECT * FROM dbo.GetWash(?,?)"; //container_id, wash_date

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(WashMapper.class);

    public WashMapper (Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a wash
     * @param containerId identifier of the container where this wash belongs
     * @param washDate date and hour when the wash was made
     * @throws SQLInvalidDependencyException if the container doesn't exists
     * @throws SQLException
     */
    public void createWash (int containerId, LocalDateTime washDate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_WASH)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(washDate));
            st.executeQuery();
        } catch (SQLException e) {
            log.warn("Error on @WashMapper.createWash ()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the container is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Creates a wash for every container contained in a collect zone for a given type
     * @param collectZoneId identifier of the collect zone that is being washed
     * @param washDate date and hour when the wash was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ssTZD
     * @param containerType type of the containers to be washed
     * @throws SQLInvalidDependencyException if the collect zone doesn't exists or doesn't have containers for the given type
     * @throws SQLException
     */
    public void washCollectZoneContainers(int collectZoneId, LocalDateTime washDate, String containerType) throws Exception {
        try (CallableStatement st = conn.get().prepareCall(WASH_COLLECT_ZONE_CONTAINERS)) {
            st.setInt(1, collectZoneId);
            st.setTimestamp(2, Timestamp.valueOf(washDate));
            st.setString(3, containerType);
            st.executeQuery();
        } catch (SQLException e) {
            log.warn("Error on @WashMapper.createWash ()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the container is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Changes the date of a wash. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param containerId identifier of the container where this wash belongs
     * @param actualWashDate the date that is stored before the update on the database
     * @param newWashDate the new date to store on database
     * @throws SQLNonExistentEntryException if the entry with the identified by the pair (@containerId, @actualWashDate)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateWash (int containerId, LocalDateTime actualWashDate, LocalDateTime newWashDate) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_WASH)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(actualWashDate));
            st.setTimestamp(3, Timestamp.valueOf(newWashDate));
            st.executeQuery();
        } catch (SQLException e) {
            log.warn("Error on @WashMapper.updateWash ()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Wash table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns a list with the washes of the container
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerId the identifier of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the washes made on the desired container
     * @throws SQLException
     */
    public PaginatedList<Wash> getContainerWashes(int pageNumber, int rowsPerPage, int containerId) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Wash> washes = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_WASHES)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, containerId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, washes);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                washes.add(new Wash(rs.getInt(CONTAINER_ID_COLUMN_NAME), rs.getTimestamp(WASH_DATE_COLUMN_NAME).toLocalDateTime()));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, washes);
        } catch (SQLException e) {
            log.warn("Error on @WashMapper.getContainerWashes()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @WashMapper.getContainerWashes()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @WashMapper.getContainerWashes() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get a container wash info
     * @param containerId identifier of the container
     * @param washDate date and time of the wash
     * @return an instance of Wash
     * @throws SQLException
     */
    public Wash getContainerWash (int containerId, LocalDateTime washDate) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_WASH)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(washDate));
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent container wash");
                return null;
            }
            return new Wash(rs.getInt(CONTAINER_ID_COLUMN_NAME),rs.getTimestamp(WASH_DATE_COLUMN_NAME).toLocalDateTime());
        } catch (SQLException e) {
            log.warn("Error on @WashMapper.getContainerWash()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @WashMapper.getContainerWash()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @WashMapper.getContainerWash() " +
                        "because it was null");
            }
        }
    }
}
