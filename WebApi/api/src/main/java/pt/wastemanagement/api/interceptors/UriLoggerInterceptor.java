package pt.wastemanagement.api.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UriLoggerInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UriLoggerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        log.info("Method {} for URI {}{} called", request.getMethod(), request.getRequestURI(), request.getQueryString() == null ? "" : "?" + request.getQueryString());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("Response with status code {}", response.getStatus());
    }
}
