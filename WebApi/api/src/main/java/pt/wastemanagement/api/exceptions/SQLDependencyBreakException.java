package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when there is no conditions to perform the delete operation.
 * e.g: the entry that we are trying to delete is associated to another one that is not deleted.
 */
public class SQLDependencyBreakException extends SQLException {
    public static final int ERROR_CODE = 55002;

    public SQLDependencyBreakException(){
        super("One of the entities that depends on this one should be disabled or eliminated first");
    }

    public SQLDependencyBreakException(String message){
        super(message);
    }
}
