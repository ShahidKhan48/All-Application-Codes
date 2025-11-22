package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested workflow is not found.
 * 
 * @author Ninja Workflow
 */
public class WorkflowNotFoundException extends SchedulerException {
    
    private static final String ERROR_CODE = "WORKFLOW_NOT_FOUND";
    
    /**
     * Creates a new WorkflowNotFoundException.
     * 
     * @param workflowName the name of the workflow that was not found
     */
    public WorkflowNotFoundException(String workflowName) {
        super(
                String.format("Workflow '%s' not found", workflowName),
                HttpStatus.NOT_FOUND,
                ERROR_CODE
        );
    }
    
    /**
     * Creates a new WorkflowNotFoundException with a cause.
     * 
     * @param workflowName the name of the workflow that was not found
     * @param cause the cause of the exception
     */
    public WorkflowNotFoundException(String workflowName, Throwable cause) {
        super(
                String.format("Workflow '%s' not found", workflowName),
                cause,
                HttpStatus.NOT_FOUND,
                ERROR_CODE
        );
    }
}

