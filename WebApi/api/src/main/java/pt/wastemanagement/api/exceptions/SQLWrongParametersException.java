package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when there is a violation of a CHECK constraint or
 * when a wrong parameter (typically, a null one), is sent to a procedure
 */
public class SQLWrongParametersException extends SQLException {
    public static final int ERROR_CODE_1 = 547; //CHECK conflict
    public static final int ERROR_CODE_2 = 515;
    public static final int ERROR_CODE_3 = 515; //One parameter was NULL
    public static final int ERROR_CODE_4 = 8114; //Conversion error - parameter on database do not corresponds to the one that was sent

    public SQLWrongParametersException(){
        super("One of the parameters is incorrect or null");
    }

    public SQLWrongParametersException(String message){
        super(message);
    }
}
