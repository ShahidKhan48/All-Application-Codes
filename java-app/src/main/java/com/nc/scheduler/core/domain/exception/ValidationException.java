package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when request validation fails.
 * 
 * @author Ninja Workflow
 */
public class ValidationException extends SchedulerException {
    
    private static final String ERROR_CODE = "VALIDATION_ERROR";
    
    /**
     * Creates a new ValidationException.
     * 
     * @param message the validation error message
     */
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
    
    /**
     * Creates a new ValidationException with a cause.
     * 
     * @param message the validation error message
     * @param cause the cause of the exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }
}

