package pt.wastemanagement.api.exceptions;

import java.sql.SQLException;

public class SQLInvalidPasswordGenerationException extends SQLException {
    public static final int ERROR_CODE_1 = 40630; //Password too short
    public static final int ERROR_CODE_2 = 40632; //Password not complex enough

    public SQLInvalidPasswordGenerationException(String message){
        super(message);
    }

    public SQLInvalidPasswordGenerationException(){
        super("Couldn't generate a valid password. Contact database administrator and report this error");
    }
}
