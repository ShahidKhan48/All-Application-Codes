package com.nc.scheduler.infra.adapter.jobrunr;

import com.nc.scheduler.core.domain.port.out.WorkflowExecutionPort;
import com.nc.scheduler.core.domain.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of WorkflowExecutionPort using JobRunr.
 * This adapter handles the JobRunr-specific @Job annotation and delegates
 * the actual execution logic to the WorkflowExecutionService.
 * 
 * @author Ninja Workflow
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobRunrExecutionAdapter implements WorkflowExecutionPort {
    
    private final WorkflowExecutionService workflowExecutionService;
    
    /**
     * Executes a workflow by delegating to the WorkflowExecutionService.
     * This method is annotated with @Job to be executed by JobRunr.
     * The actual execution logic is in the service layer, allowing for
     * multi-step execution, retries, transformations, etc.
     * 
     * @param endpoint the HTTP endpoint URL to call
     * @param payload the JSON payload to send in the request body
     * @throws RuntimeException if the workflow execution fails
     */
    @Override
    @Job(name = "Execute workflow: %0")
    public void execute(String endpoint, String payload) {
        log.debug("JobRunr executing workflow: endpoint={}", endpoint);
        workflowExecutionService.executeWorkflow(endpoint, payload);
    }
}
