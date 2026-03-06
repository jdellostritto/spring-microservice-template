package com.flipfoundry.tutorial.application.web.exception;

/**
 * Exception thrown when an error occurs during greeting operations.
 *
 * @author <a href="mailto:jim.dellostritto@gmail.com">Jim DelloStritto</a>
 * @version 1.0
 */
public class GreetingException extends RuntimeException {

    /**
     * Constructs a new GreetingException with the specified detail message.
     *
     * @param message the detail message
     */
    public GreetingException(String message) {
        super(message);
    }

    /**
     * Constructs a new GreetingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public GreetingException(String message, Throwable cause) {
        super(message, cause);
    }
}
