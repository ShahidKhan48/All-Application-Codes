package com.nc.scheduler.core.domain.port.out;

/**
 * Outgoing port for workflow scheduling operations.
 * This port defines the contract for scheduling workflows that the domain needs from external systems.
 * 
 * @author Ninja Workflow
 */
public interface WorkflowSchedulingPort {
    
    /**
     * Schedules a recurring job using the underlying scheduler.
     * 
     * @param jobName unique name identifier for the job
     * @param endpoint the HTTP endpoint URL to call
     * @param payload the JSON payload to send
     * @param cronExpression CRON expression defining the schedule
     * @return the unique identifier of the scheduled job
     */
    String scheduleRecurring(String jobName, String endpoint, String payload, String cronExpression);

    
    /**
     * Deletes a recurring job by its name.
     * 
     * @param jobName the name of the job to delete
     */
    void deleteRecurring(String jobName);
}

