package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception class for all scheduler-related exceptions.
 * All custom exceptions should extend this class.
 * 
 * @author Ninja Workflow
 */
public class SchedulerException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    private final String errorCode;
    
    /**
     * Creates a new SchedulerException.
     * 
     * @param message the error message
     * @param httpStatus the HTTP status code
     * @param errorCode the error code for programmatic handling
     */
    public SchedulerException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    /**
     * Creates a new SchedulerException with a cause.
     * 
     * @param message the error message
     * @param cause the cause of the exception
     * @param httpStatus the HTTP status code
     * @param errorCode the error code for programmatic handling
     */
    public SchedulerException(String message, Throwable cause, HttpStatus httpStatus, String errorCode) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the HTTP status code associated with this exception.
     * 
     * @return the HTTP status code
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * Returns the error code for programmatic handling.
     * 
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }
}

