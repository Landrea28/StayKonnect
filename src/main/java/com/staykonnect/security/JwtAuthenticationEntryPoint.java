package com.staykonnect.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staykonnect.common.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Punto de entrada para manejar errores de autenticación.
 * Responde con un JSON estructurado cuando falla la autenticación.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("Respondiendo con error no autorizado. Mensaje: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Unauthorized")
                .message("Credenciales inválidas o token expirado. Por favor, inicie sesión nuevamente.")
                .path(request.getRequestURI())
                .build();

        response.getOutputStream().println(objectMapper.writeValueAsString(errorResponse));
    }
}
