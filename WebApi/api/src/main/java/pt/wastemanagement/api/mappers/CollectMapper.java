package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Collect;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CollectRequester;

import javax.inject.Provider;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CollectMapper implements CollectRequester {
    public static final String
            CONTAINER_ID_COLUMN_NAME = "container_id",
            COLLECT_DATE_COLUMN_NAME = "collect_date",
            CONFIRMED_COLUMN_DATE = "confirmed",
    //Generic columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_COLLECT = "{call dbo.CreateCollect(?,?)}", //container_ID, collect_date
            COLLECT_COLLECT_ZONE_CONTAINERS = "{call dbo.CollectCollectZoneContainers(?,?,?)}", //collect_zone_id, collect_date, container_type
            UPDATE_COLLECT = "{call dbo.UpdateCollect(?,?,?)}", //container_ID, old_collect_date, new_collect_date
            GET_CONTAINER_COLLECTS = "SELECT * FROM dbo.GetContainerCollects(?,?,?)", //page, rows, container_id
            GET_CONTAINER_COLLECT = "SELECT * FROM dbo.GetCollect(?,?)"; //container_id, start_date

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(CollectMapper.class);

    public CollectMapper (Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a collect
     * @param containerId identifier of the container where this collect belongs
     * @param collectDate date and hour when the collect was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ss
     * @throws SQLInvalidDependencyException if the container doesn't exists
     * @throws SQLException
     */
    public void createCollect (int containerId, LocalDateTime collectDate) throws Exception {
        try (CallableStatement st = conn.get().prepareCall(CREATE_COLLECT)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(collectDate));
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CollectMapper.createCollect()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the container is invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Creates a collect for every container contained in a collect zone for a given type
     * @param collectZoneId identifier of the collect zone that is being collected
     * @param collectDate date and hour when the collect was made.
     *                    Timestamp MUST follow the ISO 8061 with the format
     *                    YYYY-MM-DDThh:mm:ssTZD
     * @param containerType type of the containers to be collected
     * @throws SQLInvalidDependencyException if the collect zone doesn't exists or doesn't have containers for the given type
     * @throws SQLException
     */
    public void collectCollectZoneContainers(int collectZoneId, LocalDateTime collectDate, String containerType) throws Exception {
        try (CallableStatement st = conn.get().prepareCall(COLLECT_COLLECT_ZONE_CONTAINERS)) {
            st.setInt(1, collectZoneId);
            st.setTimestamp(2, Timestamp.valueOf(collectDate));
            st.setString(3, containerType);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CollectMapper.collectCollectZoneContainers()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("The identifier of the collect zone is invalid or the container type is wrong");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Changes the date of a collect. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param containerId identifier of the container where this collect belongs
     * @param actualCollectDate the date that is stored before the update on the database
     * @param newCollectDate the new date to store on database
     * @throws SQLNonExistentEntryException if the entry with the identified by the pair (@containerId, @actualCollectDate)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateCollect (int containerId, LocalDateTime actualCollectDate, LocalDateTime newCollectDate) throws Exception {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_COLLECT)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(actualCollectDate));
            st.setTimestamp(3, Timestamp.valueOf(newCollectDate));
            st.executeQuery();
        } catch (SQLException e) {
            log.warn("Error on @CollectMapper.updateCollect ()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Collect table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns a list with the collects of the container
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param containerId the identifier of the container to search
     * @return a list with a maximum of @rowsPerPage elements that represents the collects made on the desired container
     * @throws SQLException
     */
    public PaginatedList<Collect> getContainerCollects(int pageNumber, int rowsPerPage, int containerId) throws Exception {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Collect> collects = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_COLLECTS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, containerId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, collects);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                collects.add(new Collect(rs.getInt(CONTAINER_ID_COLUMN_NAME),rs.getTimestamp(COLLECT_DATE_COLUMN_NAME).toLocalDateTime(),
                        rs.getString(CONFIRMED_COLUMN_DATE)));
            }while (rs.next());
            return new PaginatedList(totalEntries, collects);
        } catch (SQLException e) {
            log.warn("Error on @CollectMapper.getContainerCollects()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectMapper.getContainerCollects()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectMapper.getContainerCollects() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get a container collect info
     * @param containerId identifier of the container
     * @param collectDate date and time of the collect
     * @return an instance of Collect
     * @throws SQLException
     */
    public Collect getContainerCollect (int containerId, LocalDateTime collectDate) throws Exception {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONTAINER_COLLECT)) {
            st.setInt(1, containerId);
            st.setTimestamp(2, Timestamp.valueOf(collectDate));
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent container collect");
                return null;
            }
            return new Collect(rs.getInt(CONTAINER_ID_COLUMN_NAME),rs.getTimestamp(COLLECT_DATE_COLUMN_NAME).toLocalDateTime(),
                    rs.getString(CONFIRMED_COLUMN_DATE));
        } catch (SQLException e) {
            log.warn("Error on @CollectMapper.getContainerCollect()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CollectMapper.getContainerCollect()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CollectMapper.getContainerCollect() " +
                        "because it was null");
            }
        }
    }
}
