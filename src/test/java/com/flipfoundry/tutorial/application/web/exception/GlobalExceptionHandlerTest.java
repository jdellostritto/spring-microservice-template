package com.flipfoundry.tutorial.application.web.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGreetingException_returns500WithMessage() {
        var ex = new GreetingException("greeting processing failed");
        ResponseEntity<String> response = handler.handleGreetingException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("greeting processing failed");
    }

    @Test
    void handleDepartingException_returns500WithMessage() {
        var ex = new DepartingException("departing processing failed");
        ResponseEntity<String> response = handler.handleDepartingException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("departing processing failed");
    }
}
