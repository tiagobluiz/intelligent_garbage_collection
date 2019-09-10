package pt.wastemanagement.api.interceptors;

import org.apache.tomcat.websocket.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import pt.wastemanagement.api.controllers.HomeController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if(request.getRequestURI().equalsIgnoreCase(HomeController.HOME_PATH)) return true;
        String authorizationContext = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorizationContext == null) throw new AuthenticationException("Authorization header must be filled with Basic Authentication");
        return true;
    }
}
