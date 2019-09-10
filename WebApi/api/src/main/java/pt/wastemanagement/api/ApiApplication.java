package pt.wastemanagement.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import pt.wastemanagement.api.converters.StringToLocalDateTimeConverter;
import pt.wastemanagement.api.exceptions.*;
import pt.wastemanagement.api.converters.JsonHomeMessageConverter;
import pt.wastemanagement.api.interceptors.AuthenticationInterceptor;
import pt.wastemanagement.api.interceptors.UriLoggerInterceptor;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		initApplication();
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	public JsonHomeMessageConverter jsonHomeConverter() {
		return new JsonHomeMessageConverter();
	}

	@Bean
	public UriLoggerInterceptor uriLoggerInterceptor() {
		return new UriLoggerInterceptor();
	}

	@Bean
	public AuthenticationInterceptor authenticationInterceptor() {
		return new AuthenticationInterceptor();
	}

	@Bean
	public StringToLocalDateTimeConverter stringToLocalDateTimeConverter() {
		return new StringToLocalDateTimeConverter();
	}





	public static void initApplication(){
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLAlreadyExistingException.ERROR_CODE, new SQLAlreadyExistingException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLAlreadyExistentEmployeeException.ERROR_CODE, new SQLAlreadyExistentEmployeeException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLDependencyBreakException.ERROR_CODE, new SQLDependencyBreakException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLInvalidDependencyException.ERROR_CODE, new SQLInvalidDependencyException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLNonExistentEntryException.ERROR_CODE, new SQLNonExistentEntryException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLNonExistentEmployeeException.ERROR_CODE, new SQLNonExistentEmployeeException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLPermissionDeniedException.ERROR_CODE_1, new SQLPermissionDeniedException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLPermissionDeniedException.ERROR_CODE_2, new SQLPermissionDeniedException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLPermissionDeniedException.ERROR_CODE_3, new SQLPermissionDeniedException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLPermissionDeniedException.ERROR_CODE_4, new SQLPermissionDeniedException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLWrongDateException.ERROR_CODE, new SQLWrongDateException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLWrongParametersException.ERROR_CODE_1, new SQLWrongParametersException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLWrongParametersException.ERROR_CODE_2, new SQLWrongParametersException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLWrongParametersException.ERROR_CODE_3, new SQLWrongParametersException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLWrongParametersException.ERROR_CODE_4, new SQLWrongParametersException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLInvalidPasswordGenerationException.ERROR_CODE_1, new SQLInvalidPasswordGenerationException());
		ExceptionsDecoder.SQL_EXCEPTIONS_DECODER_MAP.put(SQLInvalidPasswordGenerationException.ERROR_CODE_2, new SQLInvalidPasswordGenerationException());
	}
}
