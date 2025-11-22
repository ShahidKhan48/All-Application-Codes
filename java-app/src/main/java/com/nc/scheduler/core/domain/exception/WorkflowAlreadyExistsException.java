package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when attempting to schedule a workflow that already exists.
 * 
 * @author Ninja Workflow
 */
public class WorkflowAlreadyExistsException extends SchedulerException {
    
    private static final String ERROR_CODE = "WORKFLOW_ALREADY_EXISTS";
    
    /**
     * Creates a new WorkflowAlreadyExistsException.
     * 
     * @param workflowName the name of the workflow that already exists
     */
    public WorkflowAlreadyExistsException(String workflowName) {
        super(
                String.format("Workflow '%s' already exists and is scheduled", workflowName),
                HttpStatus.CONFLICT,
                ERROR_CODE
        );
    }
    
    /**
     * Creates a new WorkflowAlreadyExistsException with a cause.
     * 
     * @param workflowName the name of the workflow that already exists
     * @param cause the cause of the exception
     */
    public WorkflowAlreadyExistsException(String workflowName, Throwable cause) {
        super(
                String.format("Workflow '%s' already exists and is scheduled", workflowName),
                cause,
                HttpStatus.CONFLICT,
                ERROR_CODE
        );
    }
}

