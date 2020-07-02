package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

public class SpringSecurityApiExceptionHandler implements ApiExceptionHandler {

    private final ErrorHandlingProperties properties;
    private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_TO_STATUS_MAPPING;

    static {
        EXCEPTION_TO_STATUS_MAPPING = new HashMap<>();
        EXCEPTION_TO_STATUS_MAPPING.put(AccessDeniedException.class, FORBIDDEN);
        EXCEPTION_TO_STATUS_MAPPING.put(AccountExpiredException.class, BAD_REQUEST);
        EXCEPTION_TO_STATUS_MAPPING.put(AuthenticationCredentialsNotFoundException.class, UNAUTHORIZED);
        EXCEPTION_TO_STATUS_MAPPING.put(AuthenticationServiceException.class, INTERNAL_SERVER_ERROR);
        EXCEPTION_TO_STATUS_MAPPING.put(BadCredentialsException.class, BAD_REQUEST);
        EXCEPTION_TO_STATUS_MAPPING.put(UsernameNotFoundException.class, BAD_REQUEST);
        EXCEPTION_TO_STATUS_MAPPING.put(InsufficientAuthenticationException.class, UNAUTHORIZED);
        EXCEPTION_TO_STATUS_MAPPING.put(LockedException.class, BAD_REQUEST);
        EXCEPTION_TO_STATUS_MAPPING.put(DisabledException.class, BAD_REQUEST);
    }

    public SpringSecurityApiExceptionHandler(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return EXCEPTION_TO_STATUS_MAPPING.containsKey(exception.getClass());
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        HttpStatus httpStatus = EXCEPTION_TO_STATUS_MAPPING.getOrDefault(exception.getClass(), INTERNAL_SERVER_ERROR);
        return new ApiErrorResponse(httpStatus,
                                    replaceCodeWithConfiguredOverrideIfPresent(exception.getClass().getName()),
                                    exception.getMessage());
    }

    private String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }

}
