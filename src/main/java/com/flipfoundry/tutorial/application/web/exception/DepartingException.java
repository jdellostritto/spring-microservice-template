package com.flipfoundry.tutorial.application.web.exception;

/**
 * Exception thrown when an error occurs during departing operations.
 *
 * @author <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 */
public class DepartingException extends RuntimeException {

    /**
     * Constructs a new DepartingException with the specified detail message.
     *
     * @param message the detail message
     */
    public DepartingException(String message) {
        super(message);
    }

    /**
     * Constructs a new DepartingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public DepartingException(String message, Throwable cause) {
        super(message, cause);
    }
}
