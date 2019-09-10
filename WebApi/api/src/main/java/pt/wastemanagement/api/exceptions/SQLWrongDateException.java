package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * Typically, this exception occurs when the date format is incorrect
 * The format of input on database is YYYY-MM-DDThh:mm:ss, but on the API, is YYYY-MM-DDThh:mm:ssTZD
 */
public class SQLWrongDateException extends SQLException {
    public static final int ERROR_CODE = 242;

    public SQLWrongDateException(){
        super("The date is incorrect. Check if the format is YYYY-MM-DDThh:mm:ssTZD");
    }

    public SQLWrongDateException(String message){
        super(message);
    }
}
