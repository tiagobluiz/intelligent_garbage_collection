package pt.wastemanagement.api.controllers;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pt.wastemanagement.api.exceptions.*;
import pt.wastemanagement.api.views.output.ProblemJson;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * The proposal of this class is to handle the exceptions that are launched in our application.
 * Some errors don't have log on their handlers because they were already logged before they reach
 * the handler itself.
 */
@ControllerAdvice
public class ExceptionsControllerAdvice {
    HttpHeaders predefinedHeaders;
    private static final Logger log = LoggerFactory.getLogger(ExceptionsControllerAdvice.class);

    public ExceptionsControllerAdvice(){
        predefinedHeaders = new HttpHeaders();
        predefinedHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    //Path partitions
    public static final String
             EXCEPTIONS_PREFIX = "/exceptions";

    //Path Suffix
    public static final String
            DATABASE_ERROR = "/database-generic-error",
            URI_ERROR = "/uri-malformation-error",
            ILLEGAL_ARGUMENTS_ERROR = "/illegal-arguments-error",
            GENERIC_ERROR = "/generic-error",
            WRONG_HTTP_METHOD_ERROR = "/not-available-http-method-error",
            WRONG_FORMAT_ERROR = "/unsupported-format-error",
            NOT_READABLE_ERROR = "/not-readable-error",
            AUTHENTICATION_ERROR="/unauthorized-error",
            INVALID_PASSWORD_GENERATION_ERROR = "/invalid-password-generation-error",
            ALREADY_EXISTING_ERROR = "/already-existing-error",
            DEPENDENCY_BREAK_ERROR = "/dependency-break-error",
            INVALID_DEPENDENCY_ERROR = "/invalid-dependency-error",
            NON_EXISTENT_ERROR = "/non-existent-error",
            PERMISSION_DENIED_ERROR = "/permission-denied-error",
            WRONG_PARAMETERS_ERROR = ILLEGAL_ARGUMENTS_ERROR;

    //Paths
    private static final String
            DATABASE_ERROR_PATH = EXCEPTIONS_PREFIX + DATABASE_ERROR,
            URI_MALFORMATION_ERROR_PATH = EXCEPTIONS_PREFIX + URI_ERROR,
            EXCEPTION_PATH = EXCEPTIONS_PREFIX + GENERIC_ERROR,
            WRONG_HTTP_METHOD_PATH = EXCEPTIONS_PREFIX + WRONG_HTTP_METHOD_ERROR,
            WRONG_FORMAT_PATH = EXCEPTIONS_PREFIX + WRONG_FORMAT_ERROR,
            NOT_READABLE_PATH = EXCEPTIONS_PREFIX + NOT_READABLE_ERROR,
            UNAUTHORIZED_PATH = EXCEPTION_PATH + AUTHENTICATION_ERROR,
            ILLEGAL_ARGUMENTS_PATH = EXCEPTIONS_PREFIX + ILLEGAL_ARGUMENTS_ERROR,
            ALREADY_EXISTING_PATH = EXCEPTIONS_PREFIX + ALREADY_EXISTING_ERROR,
            DEPENDENCY_BREAK_PATH = EXCEPTIONS_PREFIX + DEPENDENCY_BREAK_ERROR,
            INVALID_DEPENDENCY_PATH = EXCEPTIONS_PREFIX +  INVALID_DEPENDENCY_ERROR,
            NON_EXISTENT_PATH = EXCEPTIONS_PREFIX + NON_EXISTENT_ERROR,
            PERMISSION_DENIED_PATH =  EXCEPTIONS_PREFIX + PERMISSION_DENIED_ERROR,
            WRONG_PARAMETERS_PATH = EXCEPTIONS_PREFIX + WRONG_PARAMETERS_ERROR,
            INVALID_PASSWORD_GENERATION_PATH = EXCEPTIONS_PREFIX + INVALID_PASSWORD_GENERATION_ERROR;

    /**
     * DATABASE EXCEPTIONS
     */

    @ExceptionHandler({SQLWrongDateException.class,SQLWrongParametersException.class})
    public final ResponseEntity<ProblemJson> handleSQLWrongParametersException(Throwable ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        WRONG_PARAMETERS_PATH,
                        "Errors on parameters",
                        HttpStatus.BAD_REQUEST.value(),
                        "Hey! One or more parameters are wrong :/ Verify all of them and their format", ex.getMessage())
                );
    }

    @ExceptionHandler(SQLPermissionDeniedException.class)
    public final ResponseEntity<ProblemJson> handleSQLPermissionDeniedException(Throwable ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        PERMISSION_DENIED_PATH,
                        "Permission denied",
                        HttpStatus.FORBIDDEN.value(),
                        "Hey! You don't have permissions to do that action :/ " + ex.getMessage(), ex.getMessage())
                );
    }

    @ExceptionHandler({SQLNonExistentEmployeeException.class,SQLNonExistentEntryException.class})
    public final ResponseEntity<ProblemJson> handleSQLNonExistentException(Throwable ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        NON_EXISTENT_PATH,
                        "Entry not found",
                        HttpStatus.NOT_FOUND.value(),
                        "Hey! You are doing an action for something that doesn't exists... :/ " + ex.getMessage(), ex.getMessage())
                );
    }

    @ExceptionHandler(SQLInvalidDependencyException.class)
    public final ResponseEntity<ProblemJson> handleSQLInvalidDependencyException(Throwable ex) {
        return ResponseEntity.unprocessableEntity().contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        INVALID_DEPENDENCY_PATH,
                        "Invalid dependency",
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Hey! One of the dependencies are invalid :/ Verify if the keys of the introduced dependencies are correct or if" +
                                " they even exist. More details: " + ex.getMessage(), ex.getMessage())
                );
    }

    @ExceptionHandler(SQLInvalidPasswordGenerationException.class)
    public final ResponseEntity<ProblemJson> handleSQLInvalidPasswordGenerationException(Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        INVALID_PASSWORD_GENERATION_PATH,
                        "Couldn't generate a valid password",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        ex.getMessage())
                );
    }

    @ExceptionHandler(SQLDependencyBreakException.class)
    public final ResponseEntity<ProblemJson> handleSQLDependencyBreakException(Throwable ex) {
        return ResponseEntity.unprocessableEntity().contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        DEPENDENCY_BREAK_PATH,
                        "There's still alive/active dependencies",
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Hey! You can't delete/deactivate that! :/ Verify if the things that depend on the one that you're trying to delete/deactivate " +
                                "are marked as inactive and/or deleted", ex.getMessage())
                );
    }

    @ExceptionHandler({SQLAlreadyExistentEmployeeException.class,SQLAlreadyExistingException.class})
    public final ResponseEntity<ProblemJson> handleSQLAlreadyExistentException(Throwable ex) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        ALREADY_EXISTING_PATH,
                    "An entry with the given key(s) already exists!",
                    HttpStatus.BAD_REQUEST.value(),
                    "Hey! You're trying to create something that already exists... :/ " +
                            "Verify if what you want don't exists already. More details: " + ex.getMessage(), ex.getMessage())
                );
    }

    @ExceptionHandler(SQLException.class)
    public final ResponseEntity<ProblemJson> handleSQLException(Throwable ex) {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        DATABASE_ERROR_PATH,
                        "Database error",
                        HttpStatus.BAD_REQUEST.value(),
                        "Hey! Looks like an error occurred on our database :/ Try again later", ex.getMessage())
                );
    }

    /**
     *  HTTP EXCEPTIONS
     */

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public final ResponseEntity<ProblemJson> handleHttpMediaTypeNotAcceptableException (Throwable ex, HttpServletRequest request){
        log.warn("Couldn't find a representation in {} for {}", request.getHeader(HttpHeaders.ACCEPT), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemJson(
                        WRONG_FORMAT_PATH,
                        "Unacceptable format",
                        HttpStatus.NOT_ACCEPTABLE.value(),
                        "Hey! We can't represent the result in the format you want :/ Maybe you should try a different one",
                        ex.getMessage())
                );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public final ResponseEntity<ProblemJson> handleHttpMediaTypeNotSupportedException(Throwable ex, HttpServletRequest request){
        log.warn("Couldn't read a representation in {} for {}", request.getHeader(HttpHeaders.CONTENT_TYPE), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                WRONG_FORMAT_PATH,
                "Unsupported format",
                HttpStatus.NOT_ACCEPTABLE.value(),
                "Hey! We can't read the body in the format you sent us :/ Maybe you should try a different one",
                ex.getMessage())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<ProblemJson> handleHttpMessageNotReadableException(Throwable ex, HttpServletRequest request){
        log.info("Parse error at {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                NOT_READABLE_PATH,
                "Wrong syntax",
                HttpStatus.NOT_ACCEPTABLE.value(),
                "Hey! We can't read the body as you sent us :/ Maybe you should verify if you write everything correctly",
                ex.getMessage())
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public final ResponseEntity<ProblemJson> handleHttpRequestMethodNotSupportedException(Throwable ex, HttpServletRequest request){
        log.warn("Wrong method to destination path ({} - {})", request.getMethod(), request.getContextPath());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                WRONG_HTTP_METHOD_PATH,
                "Unexpected operation",
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Hey! The operation you tried is not available",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(URISyntaxException.class)
    public final ResponseEntity<ProblemJson> handleURISyntaxException(Throwable ex, HttpServletRequest request){
        log.error("URI malformation on a {} request to {}", request.getMethod(), request.getContextPath());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                URI_MALFORMATION_ERROR_PATH,
                "An URI malformation occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Hey! Looks like there's something that is not well done :( ... Please contact the administrator",
                ex.getMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public final ResponseEntity<ProblemJson> handleIllegalArgumentException(Throwable ex, HttpServletRequest request){
        log.warn("Illegal arguments on {} - {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                ILLEGAL_ARGUMENTS_PATH,
                "One or more parameters are wrong",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ex.getMessage())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public final ResponseEntity<ProblemJson> handleAuthenticationException(Throwable ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.WWW_AUTHENTICATE,"Basic")
                .body(new ProblemJson(
                        UNAUTHORIZED_PATH,
                    "Authentication error",
                    HttpStatus.UNAUTHORIZED.value(),
                    ex.getMessage(),
                    ex.getMessage())
        );
    }

    @ExceptionHandler(BeanCreationException.class)
    public final ResponseEntity<ProblemJson> handleBeanInstantiationException(Throwable ex){
        if(ex.getCause() != null && ex.getCause().getCause() instanceof SQLServerException) {
            if(((SQLServerException) ex.getCause().getCause()).getErrorCode() == SQLPermissionDeniedException.ERROR_CODE_5)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body(new ProblemJson(UNAUTHORIZED_PATH,
                                "Login failed",
                                HttpStatus.UNAUTHORIZED.value(),
                                "Login failed! Verify your username and/or password",
                                ex.getCause().getCause().getMessage()));

            /*
                This error is logged in API, but we consider that the information given by ex.getMessage(), on this case,
                could cause a security breach, since our database IP Address would be exposed to everyone
                This error occurs, usually, when there's no connection to database or when the login is not successful
             */
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                    ILLEGAL_ARGUMENTS_PATH,
                    "One or more parameters are wrong",
                    HttpStatus.BAD_REQUEST.value(),
                    "",
                    ex.getCause().getCause().getMessage())
            );
        }else if(ex.getCause() != null && ex.getCause().getCause() instanceof AuthenticationException){
            /*
                This error occurs when one or both fields of the BASIC Authorization header are not filled
             */
            log.warn(ex.getCause().getCause().getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                    UNAUTHORIZED_PATH,
                    "Wrong credentials",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    ex.getCause().getCause().getMessage(),
                    ex.getCause().getCause().getMessage())
            );
        }
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                EXCEPTION_PATH,
                "Unexpected error",
                HttpStatus.UNAUTHORIZED.value(),
                ex.getCause().getCause().getMessage(), "")
        );
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ProblemJson> handleUncaughtExceptions(Throwable ex, HttpServletRequest request){
        log.error("Exception on a {} request to {}. Exception details: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(new ProblemJson(
                EXCEPTION_PATH,
                "Unexpected error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Hey! An unexpected error occurred :/ Maybe you should try again later",
                ex.getMessage())
        );
    }
}
