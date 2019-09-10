package pt.wastemanagement.api;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.controllers.Controller;
import pt.wastemanagement.api.controllers.EmployeeController;
import pt.wastemanagement.api.mappers.EmployeeMapper;
import pt.wastemanagement.api.model.Employee;
import pt.wastemanagement.api.requester_implementations.EmployeeRequesterImplementation;
import pt.wastemanagement.api.views.input.EmployeeInput;
import pt.wastemanagement.api.views.output.collection_json.CollectionJson;
import pt.wastemanagement.api.views.output.collection_json.Property;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static pt.wastemanagement.api.requester_implementations.EmployeeRequesterImplementation.*;

public class EmployeeControllerTests {

    @Test
    public void createEmployeeTest() {
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        String username = "tita", name = "Tininha", email = "tita@wastemanagement.com", job = "Collector";
        int phone = 912388899;
        EmployeeInput employeeInput = new EmployeeInput();
        employeeInput.username = username;
        employeeInput.job = job;
        employeeInput.email = email;
        employeeInput.name = name;
        employeeInput.phoneNumber = phone;

        ResponseEntity res = null;
        try {
            res = employeeController.createEmployee(employeeInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.CREATED, res.getStatusCode());

    }

    @Test
    public void updateEmployeeJobTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        String username = "tita", job = "Collector";
        EmployeeInput employeeInput = new EmployeeInput();
        employeeInput.job = job;

        ResponseEntity res = null;

        try {
            res = employeeController.updateEmployeeJob(username, employeeInput);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }


    @Test
    public void updatePasswordTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        String username = "tita", job = "Collector";

        ResponseEntity res = null;
        try {
            res = employeeController.updatePassword(username);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());
    }

    @Test
    public void deleteEmployeeTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        String username = "tita";

        ResponseEntity res = null;
        try {
            res = employeeController.deleteEmployee(username);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
    }

    @Test
    public void getAllEmployeesTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        int page = 1, rows = 10;

        CollectionJson collection = null;
        try {
            collection = employeeController.getAllEmployees(page, rows).collection;
        } catch (Exception e) {
            fail();
        }

        String expectedURI = EmployeeController.GET_ALL_EMPLOYEES_PATH +
                "?" + Controller.PAGE_QUERY_PARAM + "=" + page + "&" + Controller.ROWS_QUERY_PARAM + "=" + rows ;

        assertEquals(expectedURI, collection.href.toString());

        List<Property> expectedTemplateProperties = new ArrayList<>();
        expectedTemplateProperties.add(new Property(EmployeeController.USERNAME_FIELD_NAME, empty(),
                of(EmployeeController.USERNAME_TITLE), empty()));
        expectedTemplateProperties.add(new Property(EmployeeController.NAME_FIELD_NAME, empty(),
                of(EmployeeController.NAME_TITLE), empty()));
        expectedTemplateProperties.add(new Property(EmployeeController.EMAIL_FIELD_NAME, empty(),
                of(EmployeeController.EMAIL_TITLE), empty()));
        expectedTemplateProperties.add(new Property(EmployeeController.PHONE_NUMBER_FIELD_NAME, empty(),
                of(EmployeeController.PHONE_NUMBER_TITLE), empty()));
        expectedTemplateProperties.add(new Property(EmployeeController.JOB_FIELD_NAME, empty(),
                of(EmployeeController.JOB_TITLE), of(EmployeeMapper.EMPLOYEE_JOBS)));

        collection.template.data.forEach(property -> {
            assertTrue(expectedTemplateProperties.stream().anyMatch(prop ->
                    prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                            Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
            ));
        });

        List<Property> expectedProperties = new ArrayList<>();
        expectedProperties.add(new Property(EmployeeController.USERNAME_FIELD_NAME, of(USERNAME),
                of(EmployeeController.USERNAME_TITLE), empty()));
        expectedProperties.add(new Property(EmployeeController.NAME_FIELD_NAME, of(NAME),
                of(EmployeeController.NAME_TITLE), empty()));
        expectedProperties.add(new Property(EmployeeController.EMAIL_FIELD_NAME, of(EMAIL),
                of(EmployeeController.EMAIL_TITLE), empty()));
        expectedProperties.add(new Property(EmployeeController.PHONE_NUMBER_FIELD_NAME, of("" + PHONE),
                of(EmployeeController.PHONE_NUMBER_TITLE), empty()));
        expectedProperties.add(new Property(EmployeeController.JOB_FIELD_NAME, of(JOB),
                of(EmployeeController.JOB_TITLE), empty()));

        assertEquals(TOTAL_EMPLOYEES, collection.items.size());

        int[] propertiesChecked = new int[]{0};
        collection.items.stream()
                .flatMap(item -> item.data.stream())
                .forEach(property ->{
                    assertTrue(expectedProperties.stream().anyMatch(prop ->
                            prop.name.equals(property.name) && Objects.equals(prop.value, property.value) &&
                                    Objects.equals(prop.options, property.options) && Objects.equals(prop.prompt, property.prompt)
                    ));
                    propertiesChecked[0]++;
                });

        assertEquals(expectedProperties.size(), propertiesChecked[0]);
    }

    @Test
    public void getEmployeeInfoTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());
        String username = "tita";

        ResponseEntity res = null;
        try {
            res = employeeController.getEmployeeInfo(username);
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());

        SirenOutput siren= (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.EMPLOYEE_CLASS, siren._class[0]);

        //Properties
        Employee employee = (Employee) siren.properties;

        assertEquals(username, employee.username);
        assertEquals(NAME, employee.name);
        assertEquals(EMAIL, employee.email);
        assertEquals(JOB, employee.job);
        assertEquals(PHONE, employee.phoneNumber);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(EmployeeController.CHANGE_EMPLOYEE_JOB_ACTION_NAME);
        expectedActionsNames.add(EmployeeController.UPDATE_PASSWORD_ACTION_NAME);
        expectedActionsNames.add(EmployeeController.DELETE_EMPLOYEE_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(EmployeeController.GET_EMPLOYEE_PATH
                .replace(EmployeeController.USERNAME_PATH_VAR, username));
        expectedLinks.add(EmployeeController.GET_ALL_EMPLOYEES_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }

    @Test
    public void getCurrentEmployeeInfoTest(){
        ApiApplication.initApplication();
        EmployeeController employeeController = new EmployeeController(new EmployeeRequesterImplementation());

        ResponseEntity res = null;
        try {
            res = employeeController.getCurrentEmployeeInfo();
        } catch (Exception e) {
            fail();
        }

        assertEquals(HttpStatus.OK, res.getStatusCode());

        SirenOutput siren= (SirenOutput) res.getBody();

        //Class
        assertEquals(Controller.EMPLOYEE_CLASS, siren._class[0]);

        //Properties
        Employee employee = (Employee) siren.properties;

        assertEquals(USERNAME, employee.username);
        assertEquals(NAME, employee.name);
        assertEquals(EMAIL, employee.email);
        assertEquals(JOB, employee.job);
        assertEquals(PHONE, employee.phoneNumber);

        //Actions
        List<String> expectedActionsNames = new ArrayList<>();
        expectedActionsNames.add(EmployeeController.CHANGE_EMPLOYEE_JOB_ACTION_NAME);
        expectedActionsNames.add(EmployeeController.UPDATE_PASSWORD_ACTION_NAME);
        expectedActionsNames.add(EmployeeController.DELETE_EMPLOYEE_ACTION_NAME);

        for (SirenAction action : siren.actions) {
            assertTrue(expectedActionsNames.contains(action.name));
            expectedActionsNames.remove(action.name);
        }

        assertEquals(0, expectedActionsNames.size());

        // Entities

        assertEquals(0, siren.entities.size());

        //Links
        List<String> expectedLinks = new ArrayList<>();
        expectedLinks.add(EmployeeController.GET_CURRENT_EMPLOYEE_PATH);
        expectedLinks.add(EmployeeController.GET_ALL_EMPLOYEES_PATH);

        siren.links.forEach(link ->{
            assertTrue(expectedLinks.contains(link.href.toString()));
            expectedLinks.remove(link.href.toString());
        });

        assertEquals(0, expectedLinks.size());
    }
}
