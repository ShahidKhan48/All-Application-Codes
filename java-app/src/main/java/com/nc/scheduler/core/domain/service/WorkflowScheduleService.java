package com.nc.scheduler.core.domain.service;

import com.nc.scheduler.core.domain.port.in.WorkflowSchedulePort;
import com.nc.scheduler.core.domain.port.out.WorkflowSchedulingPort;
import com.nc.scheduler.core.domain.util.SchedulerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service implementation of WorkflowSchedulePort.
 * This service contains the business logic for workflow scheduling, including
 * building workflow endpoint URLs and orchestrating scheduling operations.
 * 
 * @author Ninja Workflow
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowScheduleService implements WorkflowSchedulePort {
    
    private final WorkflowSchedulingPort workflowSchedulingPort;
    
    @Value("${scheduler.workflow.endpoint-base}")
    private String workflowEndpointBase;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String scheduleRecurringWorkflow(String realmId, String workflowName, String payload, String cronExpression) {
        log.debug("Scheduling recurring workflow: realm='{}', workflow='{}'", realmId, workflowName);
        String jobName = SchedulerUtils.buildJobName(realmId, workflowName);
        
        // Build workflow endpoint URL from base URL and workflow name
        String endpoint = buildWorkflowEndpoint(realmId, workflowName);
        
        // Delegate to scheduling port for actual scheduling
        return workflowSchedulingPort.scheduleRecurring(jobName, endpoint, payload, cronExpression);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRecurringWorkflow(String realmId, String workflowName) {
        log.debug("Deleting recurring workflow: realm='{}', workflow='{}'", realmId, workflowName);
        String jobName = SchedulerUtils.buildJobName(realmId, workflowName);
        workflowSchedulingPort.deleteRecurring(jobName);
    }
    
    /**
     * Builds the complete workflow endpoint URL from the base URL and workflow name.
     * This is business logic that could be extended with validation, transformation, etc.
     * 
     * @param workflowName the name of the workflow
     * @return the complete endpoint URL
     */
    private String buildWorkflowEndpoint(String realmId, String workflowName) {
        String resolvedBase = workflowEndpointBase.replace("{realmId}", realmId);
        return resolvedBase + workflowName;
    }
}

