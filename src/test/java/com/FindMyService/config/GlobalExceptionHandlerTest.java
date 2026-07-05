package com.FindMyService.config;

import com.FindMyService.utils.ResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void illegalArgumentReturnsBadRequest() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad input");
    }

    @Test
    void responseStatusExceptionReturnsItsStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        ResponseEntity<Map<String, Object>> response = handler.handleResponseStatus(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "not found");
    }

    @Test
    void authorizationDeniedReturnsForbidden() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAuthorizationDenied(new AuthorizationDeniedException("denied"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("message", "Access denied");
    }

    @Test
    void dataIntegrityViolationReturnsConflict() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("fk constraint"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void genericExceptionReturnsInternalServerError() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new RuntimeException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
