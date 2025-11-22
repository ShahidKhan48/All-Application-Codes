package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when workflow execution fails.
 * 
 * @author Ninja Workflow
 */
public class WorkflowExecutionException extends SchedulerException {
    
    private static final String ERROR_CODE = "WORKFLOW_EXECUTION_FAILED";
    
    /**
     * Creates a new WorkflowExecutionException.
     * 
     * @param endpoint the endpoint that failed to execute
     */
    public WorkflowExecutionException(String endpoint) {
        super(
                String.format("Failed to execute workflow for endpoint: %s", endpoint),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ERROR_CODE
        );
    }
    
    /**
     * Creates a new WorkflowExecutionException with a cause.
     * 
     * @param endpoint the endpoint that failed to execute
     * @param cause the cause of the exception
     */
    public WorkflowExecutionException(String endpoint, Throwable cause) {
        super(
                String.format("Failed to execute workflow for endpoint: %s", endpoint),
                cause,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ERROR_CODE
        );
    }
}

