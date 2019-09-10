package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

/**
 * This exception occurs when there is a PRIMARY KEY or UNIQUE KEY violation
 */
public class SQLAlreadyExistingException extends SQLException {
        public static final int ERROR_CODE = 2627; //Primary key

        public SQLAlreadyExistingException(){
            super("There is already an entry with the given keys");
        }

        public SQLAlreadyExistingException(String message){
            super(message);
        }
}
