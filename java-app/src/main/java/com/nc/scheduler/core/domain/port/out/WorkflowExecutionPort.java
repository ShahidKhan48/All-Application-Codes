package com.nc.scheduler.core.domain.port.out;

/**
 * Outgoing port for executing workflows.
 * This port defines the contract for workflow execution that the domain needs from external systems.
 * 
 * @author Ninja Workflow
 */
public interface WorkflowExecutionPort {
    
    /**
     * Executes a workflow by calling the specified endpoint with the given payload.
     * 
     * @param endpoint the HTTP endpoint URL to call
     * @param payload the JSON payload to send in the request body
     * @throws RuntimeException if the workflow execution fails
     */
    void execute(String endpoint, String payload);
}

