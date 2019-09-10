package pt.wastemanagement.api;


import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import pt.wastemanagement.api.converters.JsonHomeMessageConverter;
import pt.wastemanagement.api.converters.StringToLocalDateTimeConverter;
import pt.wastemanagement.api.interceptors.AuthenticationInterceptor;
import pt.wastemanagement.api.interceptors.UriLoggerInterceptor;
import pt.wastemanagement.api.views.input.UserCredentials;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;


@ComponentScan @Configuration
public class MvcConfig extends WebMvcConfigurationSupport{

    //database setup
    @Autowired
    private Environment env;
    private final String
            DATABASE_NAME = "spring.datasource.name",
            HOST_NAME = "spring.datasource.hostname",
            DATABASE_SERVER_NAME = "spring.datasource.servername",
            DATABASE_PORT = "spring.datasource.port",
            LOGIN_TIMEOUT = "spring.datasource.logintimeout";

    private final JsonHomeMessageConverter jsonHomeMessageConverter;
    public final StringToLocalDateTimeConverter stringToLocalDateTimeConverter;
    public final UriLoggerInterceptor uriLoggerInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;
    private static final Logger log = LoggerFactory.getLogger(MvcConfig.class);

    public MvcConfig(JsonHomeMessageConverter jsonHomeMessageConverter, StringToLocalDateTimeConverter stringToLocalDateTimeConverter,
                     UriLoggerInterceptor uriLoggerInterceptor, AuthenticationInterceptor authenticationInterceptor) {
        this.jsonHomeMessageConverter = jsonHomeMessageConverter;
        this.stringToLocalDateTimeConverter = stringToLocalDateTimeConverter;
        this.uriLoggerInterceptor = uriLoggerInterceptor;
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(uriLoggerInterceptor);
        registry.addInterceptor(authenticationInterceptor);
    }

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jsonHomeMessageConverter);
    }

    @Override
    protected void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateTimeConverter);
    }

    /**
     * Set database connection parameters. This parameters remain static and will be used for every user
     * that accesses our service
     */
    @Bean
    public SQLServerDataSource databaseSettings (){
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setDatabaseName(env.getProperty(DATABASE_NAME));
        ds.setServerName(env.getProperty(DATABASE_SERVER_NAME));
        ds.setPortNumber(Integer.parseInt(env.getProperty(DATABASE_PORT)));
        ds.setEncrypt(true);
        ds.setTrustServerCertificate(false);
        ds.setHostNameInCertificate(env.getProperty(HOST_NAME));
        ds.setLoginTimeout(Integer.parseInt(env.getProperty(LOGIN_TIMEOUT)));
        return ds;
    }

    @Bean @Scope("request")
    public Connection connectionsRecipe (SQLServerDataSource ds, NativeWebRequest webRequest) throws SQLException, AuthenticationException {
        UserCredentials credentials = userCredentialsResolver(webRequest);
        ds.setUser(credentials.username);
        ds.setPassword(credentials.password);

        return ds.getConnection();
    }

    /**
     * Create an UserCredentials instance with header Authorization with type BASIC
     * @param webRequest information about the request
     * @return an instance of UserCredentials
     * @throws AuthenticationException if one of the parameters was not present. E.g: username without password
     */
    public UserCredentials userCredentialsResolver(NativeWebRequest webRequest) throws AuthenticationException {
        String[] credentials = getCredentials(webRequest
                .getNativeRequest(HttpServletRequest.class)
                .getHeader("Authorization"));
        if(credentials.length != 2)
            throw new AuthenticationException("Username and/or password are invalid");
        return new UserCredentials(credentials[0], credentials[1]);
    }

    /**
     * Receives Authentication header and returns decoded credentials
     * @param authenticationHeader
     * @return A string array in which index 0 refers to username
     * and index 1 refers to password
     */
    public static String[] getCredentials(String authenticationHeader){
        if(authenticationHeader == null) return new String[]{""};
        String[] authenticationInfo = authenticationHeader.split(" ");
        String encondedCredentials = authenticationInfo[authenticationInfo.length - 1];
        String decodedCredentials = new String(Base64.getDecoder().decode(encondedCredentials),
                StandardCharsets.UTF_8);
        return decodedCredentials.split(":");
    }


}
