package com.nc.scheduler.app.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Standard error response DTO for API error responses.
 * 
 * @author Ninja Workflow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    /**
     * Error code for programmatic handling.
     */
    private String errorCode;
    
    /**
     * Human-readable error message.
     */
    private String message;
    
    /**
     * Timestamp when the error occurred.
     */
    private Instant timestamp;
    
    /**
     * HTTP status code.
     */
    private int status;
    
    /**
     * Optional path where the error occurred.
     */
    private String path;
}

