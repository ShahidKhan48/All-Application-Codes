package com.nc.scheduler.core.domain.port.in;

/**
 * Incoming port for workflow scheduling operations.
 * This port defines all use cases for scheduling and managing workflows.
 * 
 * @author Ninja Workflow
 */
public interface WorkflowSchedulePort {
    
    /**
     * Schedules a recurring workflow execution.
     *
     * @param realmId the tenant realmid is passed here
     * @param workflowName the name of the workflow to schedule
     * @param payload the JSON payload to send to the workflow
     * @param cronExpression CRON expression defining the schedule
     * @return the unique identifier of the scheduled job
     */
    String scheduleRecurringWorkflow(String realmId, String workflowName, String payload, String cronExpression);
    
    /**
     * Deletes a recurring workflow.
     *
     * @param realmId the tenant realmid is passed here
     * @param workflowName the name of the workflow to delete
     */
    void deleteRecurringWorkflow(String realmId, String workflowName);
}

