package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLDependencyBreakException;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.Configuration;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.ConfigurationRequester;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConfigurationMapper implements ConfigurationRequester {
    public static final String
            CONFIGURATION_ID_COLUMN_NAME = "configuration_id",
            CONFIGURATION_NAME_COLUMN_NAME = "configuration_name",
    //Generic columns
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    private static final String
            CREATE_CONFIGURATION = "{call dbo.CreateConfiguration(?,?)}", //configuration_name, configuration_id OUT
            UPDATE_CONFIGURATION = "{call dbo.UpdateConfiguration(?,?)}", //id, name
            DELETE_CONFIGURATION = "{call dbo.DeleteConfiguration(?)}", //configuration_id
            GET_ALL_CONFIGURATIONS = "SELECT * FROM dbo.GetAllConfigurations(?,?)", //page, rows
            GET_CONFIGURATION = "SELECT * FROM dbo.GetConfiguration(?)"; //configuration_id



    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(ConfigurationMapper.class);

    public ConfigurationMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates a new configuration with the given name
     * @param configurationName name of the new configuration
     * @return identifier of the new configuration
     * @throws SQLException
     */
    public int createConfiguration (String configurationName) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_CONFIGURATION)){
            st.setString(1, configurationName);
            st.registerOutParameter(2, Types.INTEGER);
            st.execute();
            return st.getInt(2);
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.createConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Updates the configuration. All the parameters need to be passed to the SQL Function
     * even if they keep the same.
     * @param configurationId identifier of the configuration
     * @param configurationName the new name to associate to the configuration
     * @throws SQLNonExistentEntryException if the entry with the identified by (@configurationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void updateConfiguration (int configurationId, String configurationName) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_CONFIGURATION)){
            st.setInt(1, configurationId);
            st.setString(2, configurationName);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.updateConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLNonExistentEntryException.ERROR_CODE){
                log.warn("Warning: Try to update a non existent entry on Configuration table");
                throw new SQLNonExistentEntryException();
            }
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Deletes a configuration
     * @param configurationId identifier of the configuration to be deleted
     * @throws SQLNonExistentEntryException if the entry with the identified by (@configurationId)
     *                                doesn't exists on the corresponding table
     * @throws SQLDependencyBreakException if there's still one or more containers that use this configuration
     * @throws SQLException
     */
    public void deleteConfiguration (int configurationId) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DELETE_CONFIGURATION)) {
            st.setInt(1, configurationId);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.deleteConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            if(e.getErrorCode() == SQLDependencyBreakException.ERROR_CODE)
                throw new SQLDependencyBreakException("This configuration is used by one or more containers");
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Returns a list with all the existent configurations
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements with information about the configurations
     * @throws SQLException
     */
    public PaginatedList<Configuration> getAllConfigurations (int pageNumber, int rowsPerPage) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Configuration> configurations = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ALL_CONFIGURATIONS)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if (!rs.next()) return new PaginatedList<>(totalEntries, configurations);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                configurations.add(new Configuration(rs.getInt(CONFIGURATION_ID_COLUMN_NAME),
                        rs.getString(CONFIGURATION_NAME_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, configurations);
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.getAllConfigurations()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @ConfigurationMapper.getAllConfigurations()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @ConfigurationMapper.getAllConfigurations() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get configuration info
     * @param configurationid identifier of the configuration to search
     * @return an instance of the object Configuration
     * @throws SQLException
     */
    public Configuration getConfiguration (int configurationid) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CONFIGURATION)) {
            st.setInt(1, configurationid);
            rs = st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent Configuration");
                return null;
            }
            return new Configuration(rs.getInt(CONFIGURATION_ID_COLUMN_NAME),
                    rs.getString(CONFIGURATION_NAME_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @ConfigurationMapper.getConfiguration()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on  @ConfigurationMapper.getConfiguration()");
            } catch (NullPointerException npe){
                log.error("Couldn't close the result set on  @ConfigurationMapper.getConfiguration()" +
                        "because it was null");
            }
        }
    }
}
