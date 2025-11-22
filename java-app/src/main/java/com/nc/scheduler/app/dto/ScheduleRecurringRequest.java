package com.nc.scheduler.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for scheduling a recurring workflow.
 * 
 * @author Ninja Workflow
 */
@Data
public class ScheduleRecurringRequest {
    
    /**
     * Unique name identifier for the workflow.
     */
    @NotBlank(message = "Workflow name is required")
    private String jobName;
    
    /**
     * CRON expression defining the schedule.
     */
    @NotBlank(message = "CRON expression is required")
    private String cronExpression;
    
    /**
     * JSON payload to be sent to the workflow.
     */
    @NotNull(message = "Payload is required")
    private Object payload;
}

