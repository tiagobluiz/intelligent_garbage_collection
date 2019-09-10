package pt.wastemanagement.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.views.output.ErrorOnBody;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class, in order to function properly, MUST be initialized before the API starts.
 * In this implementation, the initialization is made on ApiApplication.class
 */
public class ExceptionsDecoder {

    public final static Map<Integer, SQLException> SQL_EXCEPTIONS_DECODER_MAP = new HashMap<>();

    /**
     * Decode a SQLException thrown by a mapper. After decode the type of exception,
     * it throws
     * @param e the instance of SQLException where the exception occurred
     * @throws SQLException as instance of a specific SQLException, or raw SQLException if no match was found
     */
    public static SQLException decodeSQLException(SQLException e){
        return SQL_EXCEPTIONS_DECODER_MAP.getOrDefault(e.getErrorCode(), e);
    }
}
