package pt.wastemanagement.api.requester_implementations;

import pt.wastemanagement.api.model.Employee;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.EmployeeRequester;

import java.util.ArrayList;
import java.util.List;

public class EmployeeRequesterImplementation implements EmployeeRequester {

    public static final String USERNAME = "zeca", NAME = "Ze Camarinha", EMAIL = "zeca@wastemanagement.com", JOB = "Administrator", PASSWORD = "PUTCREAM";
    public static final int PHONE = 912388898, TOTAL_EMPLOYEES = 1;
    @Override
    public String createEmployee(String username, String name, String email, int phoneNumber, String job) throws Exception {
        return PASSWORD;  //We don't expect any SQL error here
    }

    @Override
    public void deleteEmployee(String username) throws Exception {
        return; //We don't expect any SQL error here
    }

    @Override
    public void changeEmployeeJob(String username, String newJob) throws Exception {
        return;  //We don't expect any SQL error here
    }

    @Override
    public String updatePassword(String username) throws Exception {
        return PASSWORD;  //We don't expect any SQL error here
    }

    @Override
    public Employee getEmployeeInfo(String username) throws Exception {
        return new Employee(username, NAME, EMAIL, PHONE, JOB);
    }

    @Override
    public Employee getCurrentEmployeeInfo() throws Exception {
        return new Employee(USERNAME, NAME, EMAIL, PHONE, JOB);
    }

    @Override
    public PaginatedList<Employee> getAllEmployees(int pageNumber, int rowsPerPage) throws Exception {
        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(USERNAME, NAME, EMAIL, PHONE, JOB));
        return new PaginatedList(TOTAL_EMPLOYEES, employees);
    }
}
