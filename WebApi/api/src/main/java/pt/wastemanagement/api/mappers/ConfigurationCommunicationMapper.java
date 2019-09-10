package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidDependencyException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.ConfigurationCommunication;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationCommunicationRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigurationCommunicationMapper implements ConfigurationCommunicationRequester {
    public static final String
            CONFIGURATION_ID_COLUMN_NAME = "configuration_id",
            COMMUNICATION_ID_COLUMN_NAME = "communication_id",
            COMMUNICATION_NAME_COLUMN_NAME = "communication_designation",
            VALUE_COLUMN_NAME = "value",
    //Generic columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    //Predefined communications names
    public static final String
            MAX_THRESHOLD_COMMUNICATION_NAME = "max_threshold",
            MAX_TEMPERATURE_COMMUNICATION_NAME = "max_temperature",
            COMMUNICATION_INTERVAL_COMMUNICATION_NAME = "communication_time_interval";

    private static final String
            ASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION = "{call dbo.AssociateCommunicationToTheConfiguration(?,?,?)}", //config_id, com_id, value
            DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION = "{call dbo.DisassociateCommunicationToConfiguration(?,?)}", //config_id, com_id
            GET_CONFIGURATION_COMMUNICATIONS_LIST = "SELECT * FROM dbo.GetConfigurationCommunications(?,?,?)", //page, rows, config_id
            GET_CONFIGURATION_COMMUNICATION = "SELECT * FROM dbo.GetConfigurationCommunication(?,?)", //config_id, comm_id
            GET_CONFIGURATION_COMMUNICATION_BY_NAME = "SELECT * FROM dbo.GetConfigurationCommunicationByName(?,?)"; //config_id, comm_name
    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(ConfigurationCommunicationMapper.class);

    public ConfigurationCommunicationMapper (Provider<Connection> conn) {
        this.conn = conn;
    }


    /**
     * Associates a communication to a configuration
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     * @throws SQLInvalidDependencyException if one of the identifiers (configurationId or communicationId)
     *                                      leads to an invalid entry on the database
     * @throws SQLException
     */
    public void associateCommunicationToTheConfiguration (int configurationId, int communicationId, int value) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(ASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION)) {
            st.setInt(1, configurationId);
            st.setInt(2, communicationId);
            st.setInt(3, value);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.associateCommunicationToTheConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLInvalidDependencyException.ERROR_CODE)
                throw new SQLInvalidDependencyException("One or both identifiers are invalid");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Disassociates a communication to a configuration
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     * @throws SQLNonExistentEntryException if the entry with the identified by the pair (@configurationId, @communicationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void disassociateCommunicationToTheConfiguration (int configurationId, int communicationId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DISASSOCIATE_COMMUNICATION_TO_THE_CONFIGURATION)) {
            st.setInt(1, configurationId);
            st.setInt(2, communicationId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.disassociateCommunicationToTheConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to delete a non existent entry on ConfigurationCommunication table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns the communications associated to a configuration
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @param configurationId identifier of the configuration
     * @return a list with a maximum of @rowsPerPage elements that represents the communications of a configuration
     * @throws SQLException
     */
    public PaginatedList<ConfigurationCommunication> getConfigurationCommunicationsList(int pageNumber, int rowsPerPage,
                                                                                        int configurationId) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<ConfigurationCommunication> communications = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONFIGURATION_COMMUNICATIONS_LIST)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            st.setInt(3, configurationId);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, communications);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                communications.add(new ConfigurationCommunication(rs.getInt(CONFIGURATION_ID_COLUMN_NAME),
                        rs.getInt(COMMUNICATION_ID_COLUMN_NAME), rs.getString(COMMUNICATION_NAME_COLUMN_NAME), rs.getShort(VALUE_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, communications);
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.getConfigurationCommunicationsList()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunicationsList()");
            } catch (NullPointerException npe) {
                log.warn("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunicationsList() " +
                        "because it was null");
            }
        }
    }

    /**
     * Gets a specific configuration communication info
     * @param configurationId identifier of the configuration
     * @param communicationId identifier of the communication
     * @return an instance of ConfigurationCommunication
     * @throws SQLException
     */
    public ConfigurationCommunication getConfigurationCommunication (int configurationId, int communicationId) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONFIGURATION_COMMUNICATION)){
            st.setInt(1, configurationId);
            st.setInt(2 , communicationId);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: unsuccessful try to obtain a Configuration Communication");
                return null;
            }
            return new ConfigurationCommunication(rs.getInt(CONFIGURATION_ID_COLUMN_NAME),
                    rs.getInt(COMMUNICATION_ID_COLUMN_NAME), rs.getString(COMMUNICATION_NAME_COLUMN_NAME), rs.getShort(VALUE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.getConfigurationCommunication()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunication()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunication() " +
                        "because it was null");
            }
        }
    }

    /**
     * Gets a specific configuration communication info, having the communication name
     * @param configurationId identifier of the configuration
     * @param communicationName name of the communication
     * @return an instance of ConfigurationCommunication
     * @throws SQLException
     */
    public ConfigurationCommunication getConfigurationCommunicationByName (int configurationId, String communicationName) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONFIGURATION_COMMUNICATION_BY_NAME)){
            st.setInt(1, configurationId);
            st.setString(2 , communicationName);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: unsuccessful try to obtain a Configuration Communication by name");
                return null;
            }
            return new ConfigurationCommunication(rs.getInt(CONFIGURATION_ID_COLUMN_NAME),
                    rs.getInt(COMMUNICATION_ID_COLUMN_NAME), rs.getShort(VALUE_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @CommunicationMapper.getConfigurationCommunicationByName()!" +
                    " SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunicationByName()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @CommunicationMapper.getConfigurationCommunicationByName() " +
                        "because it was null");
            }
        }
    }
}
