package com.aionn.identity.adapter.rest.exception;

import com.aionn.identity.domain.exception.IdentityErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class IdentityAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        var entity = IdentityExceptionHandler.buildError(
                HttpStatus.UNAUTHORIZED,
                IdentityErrorCode.AUTHENTICATION_REQUIRED.getDefaultMessage(),
                IdentityErrorCode.AUTHENTICATION_REQUIRED.getCode(),
                null);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), entity.getBody());
    }
}
