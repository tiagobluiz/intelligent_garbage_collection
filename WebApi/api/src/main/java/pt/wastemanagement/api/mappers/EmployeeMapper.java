package pt.wastemanagement.api.mappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.exceptions.SQLInvalidPasswordGenerationException;
import pt.wastemanagement.api.model.Employee;
import pt.wastemanagement.api.exceptions.SQLNonExistentEntryException;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.EmployeeRequester;
import pt.wastemanagement.api.views.output.Options;

import javax.inject.Provider;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class EmployeeMapper implements EmployeeRequester {

    //Employee Table
    public static final String
            USERNAME_COLUMN_NAME = "username",
            NAME_COLUMN_NAME = "name",
            EMAIL_COLUMN_NAME = "email",
            PHONE_NUMBER_COLUMN_NAME = "phone_number",
            JOB_COLUMN_NAME = "job",
    //Generic
            TOTAL_ENTRIES_COLUMN_NAME = "total_entries";

    public static final Options
            ADMINISTRATOR_JOB = new Options("Administrator", "administrator"),
            COLLECTOR_JOB = new Options("Collector", "collector"),
            MANAGEMENT_JOB = new Options("Management", "management");

    public static final List<Options> EMPLOYEE_JOBS =
            Arrays.asList(new Options[]{ADMINISTRATOR_JOB, COLLECTOR_JOB, MANAGEMENT_JOB});

    private static final String
            CREATE_EMPLOYEE = "{call dbo.CreateEmployee(?,?,?,?,?,?)}", //USERNAME, NAME, EMAIL, PHONE_NUMBER, JOB, password out
            DELETE_EMPLOYEE = "{call dbo.DeleteEmployee(?)}", //username
            UPDATE_PASSWORD = "{call dbo.UpdatePassword(?,?)}", //username, password OUT
            CHANGE_EMPLOYEE_JOB = "{call dbo.ChangeEmployeeJob(?,?)}", //username, newJob
            GET_EMPLOYEE_INFO = "SELECT * FROM dbo.GetEmployeeInfo(?)", //username
            GET_CURRENT_EMPLOYEE_INFO = "SELECT * FROM dbo.GetCurrentEmployeeInfo()",
            GET_ALL_EMPLOYEES = "SELECT * FROM dbo.GetAllEmployees(?,?)"; //page, rows


    private final Provider<Connection> conn;
    private static final Logger log = LoggerFactory.getLogger(EmployeeMapper.class);

    public EmployeeMapper(Provider<Connection> conn) {
        this.conn = conn;
    }

    /**
     * Creates an employee
     * @param username username to associate to the new employee
     * @param name name of the employee
     * @param email email of the employee
     * @param phoneNumber phone number of the employee
     * @param job job of the employee
     * @return password auto-generated
     * @throws SQLException
     */
    public String createEmployee (String username, String name, String email,
                                int phoneNumber, String job) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CREATE_EMPLOYEE)){
            st.setString(1, username);
            st.setString(2, name);
            st.setString(3, email);
            st.setInt(4, phoneNumber);
            st.setString(5, job);
            st.registerOutParameter(6, Types.NVARCHAR);
            /**
             * Since the password generated could not meet the complexity requirements, and since that is not
             * a request error, but a procedure error, we will give 3
             */
            boolean retryPasswordGeneration = true;
            int remainingRetries = 3;
            while (retryPasswordGeneration) {
                try {
                    st.execute();
                    retryPasswordGeneration = false;
                } catch (SQLException e) {
                    if(e.getErrorCode() != SQLInvalidPasswordGenerationException.ERROR_CODE_2)
                        throw e;
                    else if (e.getErrorCode() == SQLInvalidPasswordGenerationException.ERROR_CODE_2 && remainingRetries-- == 0)
                        throw new SQLInvalidPasswordGenerationException();
                }
            }
            return st.getString(6);
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.createEmployee()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Changes the password associated to the username
     * @param username username of the login to change
     * @return password auto-generated
     * @throws SQLException
     */
    public String updatePassword (String username) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(UPDATE_PASSWORD)){
            st.setString(1, username);
            st.registerOutParameter(2, Types.NVARCHAR);
            st.execute();
            return st.getString(2);
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.updatePassword()! SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Delete, definitely, an employee and their login
     * @param username username associated to the employee to be deleted
     * @throws SQLNonExistentEntryException if the entry with the identified by (@username)
     *                                     doesn't exists on the corresponding table
     * @throws SQLException
     */
    public void deleteEmployee (String username) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(DELETE_EMPLOYEE)) {
            st.setString(1, username);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.deleteEmployee()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Change employee job
     * @param username username associated to the employee
     * @param newJob new job to associate to the employee
     @throws SQLException
     */
    public void changeEmployeeJob(String username, String newJob) throws SQLException {
        try (CallableStatement st = conn.get().prepareCall(CHANGE_EMPLOYEE_JOB)) {
            st.setString(1, username);
            st.setString(2, newJob);
            st.execute();
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.changeEmployeeJob()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        }
    }

    /**
     * Get information about one employee
     * @param username username of the employee to get info
     * @return an instance of Employee
     * @throws SQLException
     */
    public Employee getEmployeeInfo (String username) throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_EMPLOYEE_INFO)) {
            st.setString(1, username);
            rs =  st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent employee");
                return null;
            }

            return new Employee(rs.getString(USERNAME_COLUMN_NAME),rs.getString(NAME_COLUMN_NAME),rs.getString(EMAIL_COLUMN_NAME),
                    rs.getInt(PHONE_NUMBER_COLUMN_NAME),rs.getString(JOB_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.getEmployeeInfo()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @EmployeeMapper.getEmployeeInfo()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @EmployeeMapper.getEmployeeInfo() " +
                        "because it was null");
            }
        }
    }

    /**
     * Get information about the current logged employee
     * @return an instance of Employee
     * @throws SQLException
     */
    public Employee getCurrentEmployeeInfo () throws SQLException {
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_CURRENT_EMPLOYEE_INFO)) {
            rs =  st.executeQuery();
            if(!rs.next()){
                log.warn("Warning: Unsuccessful try to obtain a non existent employee");
                return null;
            }

            return new Employee(rs.getString(USERNAME_COLUMN_NAME),rs.getString(NAME_COLUMN_NAME),rs.getString(EMAIL_COLUMN_NAME),
                    rs.getInt(PHONE_NUMBER_COLUMN_NAME),rs.getString(JOB_COLUMN_NAME));
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.getCurrentEmployeeInfo()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @EmployeeMapper.getCurrentEmployeeInfo(). " +
                        "More details: " + e.getSQLState() + " - " + e.getMessage());
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @EmployeeMapper.getCurrentEmployeeInfo() " +
                        "because it was null. More details: " + npe.getMessage());
            }
        }
    }

    /**
     * Gets all the employees registered on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents the employees
     * @throws SQLException
     */
    public PaginatedList<Employee> getAllEmployees (int pageNumber, int rowsPerPage) throws SQLException {
        if(pageNumber <= 0 || rowsPerPage <= 0)
            throw new IllegalArgumentException("The number of the page or the number of rows per page is invalid");
        List<Employee> employees = new ArrayList<>();
        int totalEntries = 0;
        ResultSet rs = null;
        try (PreparedStatement st = conn.get().prepareStatement(GET_ALL_EMPLOYEES)) {
            st.setInt(1, pageNumber);
            st.setInt(2, rowsPerPage);
            rs = st.executeQuery();
            if(!rs.next()) return new PaginatedList<>(totalEntries, employees);
            totalEntries = rs.getInt(TOTAL_ENTRIES_COLUMN_NAME);
            do{
                employees.add(new Employee(rs.getString(USERNAME_COLUMN_NAME),rs.getString(NAME_COLUMN_NAME),rs.getString(EMAIL_COLUMN_NAME),
                        rs.getInt(PHONE_NUMBER_COLUMN_NAME),rs.getString(JOB_COLUMN_NAME)));
            } while (rs.next());
            return new PaginatedList<>(totalEntries, employees);
        } catch (SQLException e) {
            log.warn("Error on @EmployeeMapper.getAllEmployees()! " +
                    "SQL State: " + e.getSQLState() + " Message: " + e.getMessage());
            throw ExceptionsDecoder.decodeSQLException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Couldn't close the result set on @EmployeeMapper.getAllEmployees()");
            } catch (NullPointerException npe) {
                log.error("Couldn't close the result set on @EmployeeMapper.getAllEmployees() " +
                        "because it was null");
            }
        }
    }
}
