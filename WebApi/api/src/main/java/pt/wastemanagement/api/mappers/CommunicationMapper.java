package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLDependencyBreakException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Communication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.CommunicationRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommunicationMapper implements CommunicationRequester {
    public static final String
            COMMUNICATION_ID_COLUMN_NAME = "communication_id",
            COMMUNICATION_NAME_COLUMN_NAME = "communication_designation",
    //generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_COMMUNICATION = "{call dbo.CreateCommunication(?,?)}", //designation, communicationId OUT
            UPDATE_COMMUNICATION = "{call dbo.UpdateCommunication(?,?)}", //id, designation
            DELETE_COMMUNICATION = "{call dbo.DeleteCommunication(?)}", //id
            GET_ALL_COMMUNICATIONS = "SELECT * FROM dbo.GetAllCommunications(?,?)", //page, rows
            GET_COMMUNICATION = "SELECT * FROM dbo.GetCommunication(?)"; //communication_id

    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(CommunicationMapper.class);

    public CommunicationMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new communication
     * @param communicationDesignation designation of the new communication
     * @return the identifier of the created communication
     * @throws SQLException
     */
    public int createCommunication (String communicationDesignation) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_COMMUNICATION)){
            st.setString(1, communicationDesignation);
            st.registerOutParameter(2, Types.INTEGER);
            st.execute();
            return st.getInt(2);
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.createCommunication()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates the communication. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param communicationId identifier of the communication
     * @param communicationDesignation the new name to associate to the communication
     * @throws SQLNonExistentEntryException if the entry with the identified by (@communicationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateCommunication (int communicationId, String communicationDesignation) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_COMMUNICATION)){
            st.setInt(1, communicationId);
            st.setString(2, communicationDesignation);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.updateCommunication()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Communication table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deletes the communication. This operation will not succeed if there's any configuration that uses the
     * communication identified by @communicationId
     * @param communicationId identifier of the communication to deleted
     * @throws SQLNonExistentEntryException if the entry with the identified by (@communicationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLDependencyBreakException if there's still one or more configurations that use this communication
     * @throws SQLException
     */
    public void deleteCommunication (int communicationId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DELETE_COMMUNICATION)){
            st.setInt(1, communicationId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.deleteCommunication()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to delete a non existent entry on Communication table");
                throw new SQLNonExistentEntryException();
            } else if(e.getErrorCode() == SQLDependencyBreakException.ERROR_CODE)
                throw new SQLDependencyBreakException("This communication is used by one or more configurations");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Get all existent communications
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents the communications
     * @throws SQLException
     */
    public PaginatedList<Communication> getAllCommunications(int pageNumber, int rowsPerPage) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Communication> routes = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try(PreparedStatement st = conn.get().prepareStatement(GET_ALL_COMMUNICATIONS)){
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, routes);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                routes.add(new Communication(rs.getInt(COMMUNICATION_ID_COLUMN_NAME),
                        rs.getString(COMMUNICATION_NAME_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, routes);
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.getAllCommunications()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CommunicationMapper.getAllCommunications()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on @CommunicationMapper.getAllCommunications() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get communication info
     * @param communicationId identifier of the communication to search
     * @return an instance of ContainerStatistics
     * @throws SQLException
     */
    public Communication getCommunication(int communicationId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_COMMUNICATION)) {
            st.setInt(1, communicationId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent communication");
                return null;
            }
            return new Communication(rs.getInt(COMMUNICATION_ID_COLUMN_NAME),
                    rs.getString(COMMUNICATION_NAME_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.getCommunication()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CommunicationMapper.getCommunication()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CommunicationMapper.getCommunication() " +
                        "because it was null");
            }
        }
    }


}
