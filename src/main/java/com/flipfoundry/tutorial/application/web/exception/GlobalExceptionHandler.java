package com.flipfoundry.tutorial.application.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * <p>Global exception handler for all controllers.</p>
 *
 * @author  <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 * @since 2025-12-14
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * <p>Exception handler for DepartingException.</p>
     *
     * @param e the DepartingException
     * @return ResponseEntity with error message and 500 status
     * @since 1.0
     */
    @ExceptionHandler(DepartingException.class)
    public ResponseEntity<String> handleDepartingException(DepartingException e) {
        logger.error("DepartingException caught: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

    /**
     * <p>Exception handler for GreetingException.</p>
     *
     * @param e the GreetingException
     * @return ResponseEntity with error message and 500 status
     * @since 1.0
     */
    @ExceptionHandler(GreetingException.class)
    public ResponseEntity<String> handleGreetingException(GreetingException e) {
        logger.error("GreetingException caught: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
    }

}
