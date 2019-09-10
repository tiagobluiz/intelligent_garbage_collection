package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when we try to perform a update or delete operation on a non existent entry.
 */
public class SQLNonExistentEntryException extends SQLException {
    public static final int ERROR_CODE = 55003;

    public SQLNonExistentEntryException(){
        super("The entry identified by the given key(s) doesn't exists");
    }


    public SQLNonExistentEntryException(String message){
        super(message);
    }

}
