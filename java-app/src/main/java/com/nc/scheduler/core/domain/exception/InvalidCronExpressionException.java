package com.nc.scheduler.core.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an invalid CRON expression is provided.
 * 
 * @author Ninja Workflow
 */
public class InvalidCronExpressionException extends SchedulerException {
    
    private static final String ERROR_CODE = "INVALID_CRON_EXPRESSION";
    
    /**
     * Creates a new InvalidCronExpressionException.
     * 
     * @param cronExpression the invalid CRON expression
     */
    public InvalidCronExpressionException(String cronExpression) {
        super(
                String.format("Invalid CRON expression: %s", cronExpression),
                HttpStatus.BAD_REQUEST,
                ERROR_CODE
        );
    }
    
    /**
     * Creates a new InvalidCronExpressionException with a cause.
     * 
     * @param cronExpression the invalid CRON expression
     * @param cause the cause of the exception
     */
    public InvalidCronExpressionException(String cronExpression, Throwable cause) {
        super(
                String.format("Invalid CRON expression: %s", cronExpression),
                cause,
                HttpStatus.BAD_REQUEST,
                ERROR_CODE
        );
    }
}

