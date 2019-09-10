package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

public class SQLNonExistentEmployeeException extends SQLException {
    public static final int ERROR_CODE = 15151;

    public SQLNonExistentEmployeeException(){
        super("The employee identified by the given username doesn't exists");
    }


    public SQLNonExistentEmployeeException(String message){
        super(message);
    }

}
