package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when the foreign key does not correspond to an existing entry on database
 */
public class SQLInvalidDependencyException extends SQLException {
    public static final int ERROR_CODE = 55001;

    public SQLInvalidDependencyException(String message){
        super(message);
    }

    public SQLInvalidDependencyException(){
        super("One of the dependencies is invalid or doesn't exists");
    }
}
