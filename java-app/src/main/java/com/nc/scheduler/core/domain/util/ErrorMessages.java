package com.nc.scheduler.core.domain.util;

import lombok.experimental.UtilityClass;

/**
 * Constants for error messages used across the application.
 * 
 * @author Ninja Workflow
 */
@UtilityClass
public final class ErrorMessages {
    
    // Workflow scheduling errors
    public static final String WORKFLOW_ALREADY_EXISTS = "Workflow '%s' already exists and is scheduled";
    public static final String WORKFLOW_NOT_FOUND = "Workflow '%s' not found";
    public static final String FAILED_TO_SCHEDULE_WORKFLOW = "Failed to schedule workflow '%s'";
    public static final String FAILED_TO_UPDATE_WORKFLOW = "Failed to update workflow '%s'";
    public static final String FAILED_TO_DELETE_WORKFLOW = "Failed to delete workflow '%s'";
    
    // Validation errors
    public static final String INVALID_JSON_PAYLOAD = "Invalid JSON payload";
    public static final String INVALID_CRON_EXPRESSION = "Invalid CRON expression: %s";
    public static final String INVALID_WORKFLOW_NAME = "Invalid workflow name: %s";
    
    // Execution errors
    public static final String WORKFLOW_EXECUTION_FAILED = "Failed to execute workflow for endpoint: %s";
    
    // Generic errors
    public static final String INTERNAL_SERVER_ERROR = "An internal server error occurred";
    public static final String INVALID_REQUEST = "Invalid request";

}

