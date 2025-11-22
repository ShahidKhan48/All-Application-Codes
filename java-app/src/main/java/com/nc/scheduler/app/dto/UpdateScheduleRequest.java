package com.nc.scheduler.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for updating an existing recurring workflow schedule.
 * 
 * @author Ninja Workflow
 */
@Data
public class UpdateScheduleRequest {
    
    /**
     * Unique name identifier for the workflow to update.
     */
    @NotBlank(message = "Workflow name is required")
    private String jobName;
    
    /**
     * New CRON expression defining the schedule.
     */
    @NotBlank(message = "CRON expression is required")
    private String cronExpression;
    
    
    /**
     * JSON payload to be sent to the workflow.
     */
    @NotNull(message = "Payload is required")
    private Object payload;
}

