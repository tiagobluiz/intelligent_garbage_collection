package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when the users that tries to perform an action does not have the correct permissions
 * to do it or if the user gives wrong credentials
 */
public class SQLPermissionDeniedException extends SQLException{
    public static final int ERROR_CODE_1 = 55004;
    public static final int ERROR_CODE_2 = 229; //User role on database without permissions
    public static final int ERROR_CODE_3 = 15247; //User role on database without permissions
    public static final int ERROR_CODE_4 = 102;   //Wrong characters in username on login creation
    public static final int ERROR_CODE_5 = 18456;   //Wrong characters in username or password when logging in

    public SQLPermissionDeniedException(){
        super("Wrong permissions and/or credentials");
    }

    public SQLPermissionDeniedException(String message){
        super(message);
    }
}
