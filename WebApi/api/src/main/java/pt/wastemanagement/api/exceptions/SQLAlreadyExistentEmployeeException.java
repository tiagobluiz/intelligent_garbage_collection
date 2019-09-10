package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

public class SQLAlreadyExistentEmployeeException extends SQLException {
    public static final int ERROR_CODE = 15025;

    public SQLAlreadyExistentEmployeeException(){
        super("There is already an employee with that username");
    }

    public SQLAlreadyExistentEmployeeException(String message){
        super(message);
    }
}
