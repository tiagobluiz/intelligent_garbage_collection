package pt.wastemanagement.api.requesters;

import pt.wastemanagement.api.model.Employee;
import pt.wastemanagement.api.model.utils.PaginatedList;

public interface EmployeeRequester {

    /**
     * Creates an employee
     * @param username username to associate to the new employee
     * @param name name of the employee
     * @param email email of the employee
     * @param phoneNumber phone number of the employee
     * @param job job of the employee
     * @return password auto-generated
     */
    String createEmployee (String username, String name, String email,
                                  int phoneNumber, String job) throws Exception;

    /**
     * Delete, definitely, an employee and their login
     * @param username username associated to the employee to be deleted
     */
    void deleteEmployee (String username) throws Exception;

    /**
     * Change employee job
     * @param username username associated to the employee
     * @param newJob new job to associate to the employee
     */
    void changeEmployeeJob(String username, String newJob) throws Exception;

    /**
     * Changes the password associated to the username
     * @param username username of the login to change
     * @return password auto-generated
     */
    String updatePassword (String username) throws Exception;

    /**
     * Get information about one employee
     * @param username username of the employee to get info
     * @return an instance of Employee
     */
    Employee getEmployeeInfo (String username) throws Exception;

    /**
     * Get information about the current logged employee
     * @return an instance of Employee
     */
    Employee getCurrentEmployeeInfo () throws Exception;

    /**
     * Gets all the employees registered on the system
     * @param pageNumber number of the page to return. Need to be greater then 0
     * @param rowsPerPage number of rows returned on the required page. Need to be greater then 0
     * @return a list with a maximum of @rowsPerPage elements that represents the employees
     */
    PaginatedList<Employee> getAllEmployees (int pageNumber, int rowsPerPage) throws Exception;
}
