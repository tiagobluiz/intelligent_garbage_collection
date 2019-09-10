package pt.wastemanagement.api.controllers;

import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.errors.MailjetSocketTimeoutException;
import com.mailjet.client.resource.Email;
import org.apache.commons.validator.routines.EmailValidator;
import org.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.wastemanagement.api.exceptions.ExceptionsDecoder;
import pt.wastemanagement.api.mappers.EmployeeMapper;
import pt.wastemanagement.api.model.Employee;
import pt.wastemanagement.api.model.utils.PaginatedList;
import pt.wastemanagement.api.requesters.EmployeeRequester;
import pt.wastemanagement.api.views.input.EmployeeInput;
import pt.wastemanagement.api.views.output.AccountOutput;
import pt.wastemanagement.api.views.output.collection_json.*;
import pt.wastemanagement.api.views.output.collection_json.Property;
import pt.wastemanagement.api.views.output.siren.Field;
import pt.wastemanagement.api.views.output.siren.SirenAction;
import pt.wastemanagement.api.views.output.siren.SirenLink;
import pt.wastemanagement.api.views.output.siren.SirenOutput;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.springframework.http.HttpStatus.*;
import static pt.wastemanagement.api.controllers.Controller.*;
import static pt.wastemanagement.api.views.output.collection_json.CollectionPlusJson.COLLECTION_PLUS_JSON_MEDIA_TYPE;
import static pt.wastemanagement.api.views.output.siren.SirenOutput.SIREN_OUTPUT_MEDIA_TYPE;


@RestController
public class EmployeeController {

    //Fields
    public static final String
            USERNAME_FIELD_NAME = "username",
            NAME_FIELD_NAME = "name",
            EMAIL_FIELD_NAME = "email",
            PHONE_NUMBER_FIELD_NAME = "phoneNumber",
            JOB_FIELD_NAME = "job";

    //Field Titles
    public static final String
            USERNAME_TITLE = "Username",
            NAME_TITLE = "Name",
            EMAIL_TITLE = "Email",
            PHONE_NUMBER_TITLE = "Phone number",
            JOB_TITLE = "Job";

    //Email
    private static final String
            WELCOME_SUBJECT = "Welcome to our organization!",
            WELCOME_CONTENT_CONTENT = "Welcome to our organization %s! In order to accomplish your functions, we deliver " +
                    "to you the password you need to use in order to entry in the system %s. Your username is %s.",
            THANK_YOU_FOR_YOUR_SERVICES_SUBJECT = "Thank you for your services",
            THANK_YOU_FOR_YOUR_SERVICES_CONTENT = "Than you for your services %s. Hope you keep well.",
            PASSWORD_CHANGED_SUBJECT = "Your credentials were updated",
            PASSWORD_CHANGED_CONTENT = "Your credentials were updated. Your username is %s and your new password is %s.";

    //Path Vars
    public static final String
            USERNAME_PATH_VAR_NAME = "username",
            USERNAME_PATH_VAR = "{" + USERNAME_PATH_VAR_NAME + "}";

    //Path partitions
    private static final String
            EMPLOYEES_PREFIX = "/employees",
            JOB_PREFIX = "/job",
            CREDENTIALS_PREFIX = "/credentials",
            CURRENT_PREFIX = "/current";

    //Paths
    public static final String
    // /employees
            GET_ALL_EMPLOYEES_PATH = EMPLOYEES_PREFIX,
            CREATE_EMPLOYEE_PATH = GET_ALL_EMPLOYEES_PATH,
    // /employees/...
    GET_CURRENT_EMPLOYEE_PATH = GET_ALL_EMPLOYEES_PATH + CURRENT_PREFIX,
    // /employees/{username}
            GET_EMPLOYEE_PATH = EMPLOYEES_PREFIX + "/" + USERNAME_PATH_VAR,
            DELETE_EMPLOYEE_PATH = GET_EMPLOYEE_PATH,
    // /employees/{username}/...
            CHANGE_EMPLOYEE_JOB_PATH = GET_EMPLOYEE_PATH + JOB_PREFIX,
            UPDATE_PASSWORD_PATH = GET_EMPLOYEE_PATH + CREDENTIALS_PREFIX;

    private final EmployeeRequester employeeRequester;
    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeRequester employeeRequester) {
        this.employeeRequester = employeeRequester;
    }

    @PostMapping(CREATE_EMPLOYEE_PATH)
    public ResponseEntity createEmployee(@RequestBody EmployeeInput employeeInput) throws Exception {
        if(!EmailValidator.getInstance().isValid(employeeInput.email))
            throw new IllegalArgumentException("Invalid email");
        if(!isEmployeeJobCorrect(employeeInput.job)) {
            log.info("Employee job received was invalid: {}", employeeInput.job);
            throw new IllegalArgumentException("Employee job is invalid. Verify if the job is one of these: " + EmployeeMapper.EMPLOYEE_JOBS.toString());
        }
        if(!isPhoneNumberValid(employeeInput.phoneNumber))
            throw new IllegalArgumentException("Invalid phone number");

        String password = employeeRequester.createEmployee(employeeInput.username, employeeInput.name, employeeInput.email,
                employeeInput.phoneNumber, employeeInput.job);
        boolean errorWhileSending = false;
        try{
            sendEmail(employeeInput.email, WELCOME_SUBJECT, String.format(WELCOME_CONTENT_CONTENT, employeeInput.name, password, employeeInput.username));
        } catch (MailjetSocketTimeoutException mste){
            log.error("An error occurred when sending an email. Details {}", mste.getMessage());
            errorWhileSending = true;
        } catch (MailjetException me){
            log.error("An error occurred when sending an email. Details {}", me.getMessage());
            errorWhileSending = true;
        }
        List<String> headers = new ArrayList<>();
        headers.add(GET_EMPLOYEE_PATH.replace(USERNAME_PATH_VAR,"" + employeeInput.username));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put(HttpHeaders.LOCATION,headers);
        /**
         * When occurs an error while sending the email that is supposed to communicate to the new employee his password,
         * a second plan must be used. The option here is to give the administrator that creates the account a temporary password.
         * This password SHOULD be changed by the employee when he signs in for the first time
         */
        if(errorWhileSending)
            return ResponseEntity.status(CREATED).body(new AccountOutput(true, "Couldn't sent email with password to employee. " +
                    "The employee SHOULD change the password as soon as possible.", password));
        return new ResponseEntity(httpHeaders, CREATED);
    }

    @PutMapping(CHANGE_EMPLOYEE_JOB_PATH)
    public ResponseEntity updateEmployeeJob(@PathVariable(USERNAME_PATH_VAR_NAME) String username, @RequestBody EmployeeInput employeeInput) throws Exception {
        if(!isEmployeeJobCorrect(employeeInput.job)) {
            log.info("Employee job received was invalid: {}", employeeInput.job);
            throw new IllegalArgumentException("Employee job is invalid. Verify if the job is one of these: " + EmployeeMapper.EMPLOYEE_JOBS.toString());
        }

        employeeRequester.changeEmployeeJob(username, employeeInput.job);
        return new ResponseEntity(OK);
    }

    @PutMapping(UPDATE_PASSWORD_PATH)
    public ResponseEntity updatePassword(@PathVariable(USERNAME_PATH_VAR_NAME) String username) throws Exception {
        Employee employee = employeeRequester.getEmployeeInfo(username); //Request database info in order to be able to send an email
        if(employee == null) return new ResponseEntity(NOT_FOUND);

        String password = employeeRequester.updatePassword(username);
        boolean errorWhileSending = false;
        try{
            sendEmail(employee.email, PASSWORD_CHANGED_SUBJECT, String.format(PASSWORD_CHANGED_CONTENT, username, password));
        } catch (MailjetSocketTimeoutException mste){
            log.error("An error occurred when sending an email. Details {}", mste.getMessage());
            errorWhileSending = true;
        } catch (MailjetException me){
            log.error("An error occurred when sending an email. Details {}", me.getMessage());
            errorWhileSending = true;
        }

        /**
         * When occurs an error while sending the email that is supposed to communicate to the employee his password,
         * a second plan must be used. The option here is to give the employee a new password that should be temporary.
         * This password SHOULD be changed by the employee when he signs in again
         */
        if(errorWhileSending)
            return ResponseEntity.status(OK).body(new AccountOutput(true, "Couldn't sent email with password to employee. " +
                    "The employee SHOULD change the password as soon as possible.", password));
        return ResponseEntity.ok(new AccountOutput(false));
    }

    @DeleteMapping(DELETE_EMPLOYEE_PATH)
    public ResponseEntity deleteEmployee(@PathVariable(USERNAME_PATH_VAR_NAME) String username) throws Exception {
        Employee employee = employeeRequester.getEmployeeInfo(username); //Request database info in order to be able to send an email
        if(employee == null) return new ResponseEntity(NOT_FOUND);

        employeeRequester.deleteEmployee(username);

        try{
            sendEmail(employee.email, THANK_YOU_FOR_YOUR_SERVICES_SUBJECT, String.format(THANK_YOU_FOR_YOUR_SERVICES_CONTENT, employee.name));
        } catch (MailjetSocketTimeoutException mste){
            log.error("An error occurred when sending an email. Details {}", mste.getMessage());
        } catch (MailjetException me){
            log.error("An error occurred when sending an email. Details {}", me.getMessage());
        }

        return new ResponseEntity(NO_CONTENT);
    }

    @GetMapping(value = GET_ALL_EMPLOYEES_PATH, produces = COLLECTION_PLUS_JSON_MEDIA_TYPE)
    public CollectionPlusJson getAllEmployees(@RequestParam(value = PAGE_QUERY_PARAM, defaultValue = "1" ) int pageNumber,
                                              @RequestParam(value=ROWS_QUERY_PARAM,  defaultValue = "20") int rowsPerPage) throws Exception {
        PaginatedList<Employee> employees =
                employeeRequester.getAllEmployees(pageNumber, rowsPerPage);

        String selfURIString = GET_ALL_EMPLOYEES_PATH;
        String selfURIWithParams = selfURIString + "?" + PAGE_QUERY_PARAM + "=" + pageNumber + "&" + ROWS_QUERY_PARAM + "=" +
                rowsPerPage;
        return new CollectionPlusJson(new CollectionJson(
                new URI(selfURIWithParams),
                getPageLinks(employees.totalEntries, selfURIString,pageNumber, rowsPerPage),
                extractEmployeesItems(employees.elements),
                new ArrayList<>(),
                of(getEmployeeTemplate())
        ));
    }

    @GetMapping(value = GET_EMPLOYEE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getEmployeeInfo(@PathVariable(USERNAME_PATH_VAR_NAME) String username) throws Exception {
        Employee employee = employeeRequester.getEmployeeInfo(username);

        if(employee == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(new SirenOutput(of(employee), EMPLOYEE_CLASS)
                .addAction(getUpdatePasswordAction(username))
                .addAction(getChangeEmployeeJobAction(username))
                .addAction(getDeleteEmployeeAction(username))
                .addLink(getEmployeeSelfLink(username))
                .addLink(getEmployeesListUpLink()),
                OK);
    }

    @GetMapping(value = GET_CURRENT_EMPLOYEE_PATH, produces = SIREN_OUTPUT_MEDIA_TYPE)
    public ResponseEntity getCurrentEmployeeInfo() throws Exception {
        Employee employee = employeeRequester.getCurrentEmployeeInfo();

        if(employee == null) return new ResponseEntity(NOT_FOUND);

        return new ResponseEntity(new SirenOutput(of(employee), EMPLOYEE_CLASS)
                .addAction(getUpdatePasswordAction(employee.username))
                .addAction(getChangeEmployeeJobAction(employee.username))
                .addAction(getDeleteEmployeeAction(employee.username))
                .addLink(getCurrentEmployeeSelfLink())
                .addLink(getEmployeesListUpLink()),
                OK);
    }


    /**
     * Utility methods
     */

    private List<Item> extractEmployeesItems(List<Employee> employees) throws URISyntaxException {
        List<Item> items = new ArrayList<>(employees.size());
        for (Employee e : employees) {
            Item item = new Item(new URI(GET_EMPLOYEE_PATH.replace(USERNAME_PATH_VAR, "" + e.username)))
                    .addProperty(new Property(USERNAME_FIELD_NAME, of(e.username), of(USERNAME_TITLE), empty()))
                    .addProperty(new Property(NAME_FIELD_NAME, of(e.name),of(NAME_TITLE), empty()))
                    .addProperty(new Property(EMAIL_FIELD_NAME, of(e.email),of(EMAIL_TITLE), empty()))
                    .addProperty(new Property(PHONE_NUMBER_FIELD_NAME, of("" + e.phoneNumber),of(PHONE_NUMBER_TITLE), empty()))
                    .addProperty(new Property(JOB_FIELD_NAME, of(e.job),of(JOB_TITLE), empty()));
            items.add(item);
        }
        return items;
    }
    /**
     * Send an email to an employee
     * @param to email of the employee
     * @param subject subject of the email to be sent
     * @param content message/content of the email
     * @throws MailjetSocketTimeoutException
     * @throws MailjetException
     */
    public static void sendEmail(String to, String subject, String content) throws MailjetSocketTimeoutException, MailjetException {
        log.info("Email with subject {} send to {}", subject, to);
        MailjetClient client;
        MailjetRequest request;
        client = new MailjetClient(API_KEY, API_SECRET);
        request = new MailjetRequest(Email.resource)
                .property(Email.FROMEMAIL, SENDER_EMAIL)
                .property(Email.FROMNAME, SENDER_NAME)
                .property(Email.SUBJECT, subject)
                .property(Email.TEXTPART, content)
                .property(Email.RECIPIENTS, new JSONArray()
                        .put(new JSONObject()
                                .put("Email", to)));
        client.post(request);
    }

    /**
     * This implementation is restricted to Portuguese phones, this implementation MUST be changed in order to use this API in other countries
     * @param phoneNumber phone number to validate
     * @return true if valid, false if not
     */
    private static boolean isPhoneNumberValid(int phoneNumber){
        String phoneString = "" + phoneNumber;
        return phoneString.length() == 9 &&  (phoneString.startsWith("91") || phoneString.startsWith("92") || phoneString.startsWith("93") || phoneString.startsWith("96"));
    }

    private static boolean isEmployeeJobCorrect(String job){
        return EmployeeMapper.EMPLOYEE_JOBS.stream()
                .filter(field -> field.value.equalsIgnoreCase(job))
                .findFirst()
                .isPresent();
    }

    /**
     * Template
     */
    public static Template getEmployeeTemplate(){
        return new Template()
                .addProperty(new Property(USERNAME_FIELD_NAME, empty(), of(USERNAME_TITLE), empty()))
                .addProperty(new Property(NAME_FIELD_NAME, empty(),of(NAME_TITLE), empty()))
                .addProperty(new Property(EMAIL_FIELD_NAME, empty(),of(EMAIL_TITLE), empty()))
                .addProperty(new Property(PHONE_NUMBER_FIELD_NAME, empty(),of(PHONE_NUMBER_TITLE), empty()))
                .addProperty(new Property(JOB_FIELD_NAME, empty(),of(JOB_TITLE), of(EmployeeMapper.EMPLOYEE_JOBS)));
    }

    /**
     * Siren Actions
     */

    //Action names
    public static final String
            CHANGE_EMPLOYEE_JOB_ACTION_NAME = "change-employee-job",
            UPDATE_PASSWORD_ACTION_NAME = "update-password",
            DELETE_EMPLOYEE_ACTION_NAME = "delete-employee";

    private static SirenAction getUpdatePasswordAction(String username) throws URISyntaxException {
        return new SirenAction(
                UPDATE_PASSWORD_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(UPDATE_PASSWORD_PATH.replace(USERNAME_PATH_VAR, username)), MediaType.ALL
        );
    }

    private static SirenAction getChangeEmployeeJobAction(String username) throws URISyntaxException {
        return new SirenAction(
                CHANGE_EMPLOYEE_JOB_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(CHANGE_EMPLOYEE_JOB_PATH.replace(USERNAME_PATH_VAR, username)), MediaType.APPLICATION_JSON
        )
                .addField(new Field(JOB_FIELD_NAME, empty(), of(NUMBER_DATA_TYPE), empty(), of(JOB_TITLE), of(EmployeeMapper.EMPLOYEE_JOBS)));
    }

    private static SirenAction getDeleteEmployeeAction(String username) throws URISyntaxException {
        return new SirenAction(
                DELETE_EMPLOYEE_ACTION_NAME, empty(), HttpMethod.PUT,
                new URI(DELETE_EMPLOYEE_PATH.replace(USERNAME_PATH_VAR, username)), MediaType.ALL
        );
    }

    /**
     * Siren Links
     */

    private static SirenLink getEmployeeSelfLink(String username) throws URISyntaxException {
        return new SirenLink(
                new URI(GET_EMPLOYEE_PATH.replace(USERNAME_PATH_VAR, username)),
                SirenLink.SELF_REL
        );
    }

    private static SirenLink getCurrentEmployeeSelfLink() throws URISyntaxException {
        return new SirenLink(
                new URI(GET_CURRENT_EMPLOYEE_PATH),
                SirenLink.SELF_REL
        );
    }



    private static SirenLink getEmployeesListUpLink() throws URISyntaxException {
        return new SirenLink(new URI(GET_ALL_EMPLOYEES_PATH), SirenLink.UP_REL);
    }
}
