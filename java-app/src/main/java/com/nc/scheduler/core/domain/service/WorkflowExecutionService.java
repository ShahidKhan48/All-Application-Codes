package com.nc.scheduler.core.domain.service;

import com.nc.scheduler.core.domain.exception.WorkflowExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service for executing workflows.
 * This service contains the business logic for workflow execution, which could include
 * multiple steps, retries, transformations, etc.
 * 
 * @author Ninja Workflow
 */
@Service
@Slf4j
public class WorkflowExecutionService {
    
    /**
     * Executes a workflow by making an HTTP POST request to the specified endpoint.
     * This method contains the execution logic and can be extended with additional steps,
     * retries, error handling, transformations, etc.
     * 
     * @param endpoint the HTTP endpoint URL to call
     * @param payload the JSON payload to send in the request body
     * @throws WorkflowExecutionException if the workflow execution fails
     */
    public void executeWorkflow(String endpoint, String payload) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json");
            
            HttpRequest request = (payload != null && !payload.isBlank())
                    ? builder.POST(HttpRequest.BodyPublishers.ofString(payload)).build()
                    : builder.POST(HttpRequest.BodyPublishers.noBody()).build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            log.debug("Workflow execution triggered: endpoint={}, status={}", endpoint, response.statusCode());
            log.debug("Workflow execution response body: {}", response.body());
            
        } catch (Exception e) {
            log.error("Failed to execute workflow: endpoint={}", endpoint, e);
            throw new WorkflowExecutionException(endpoint, e);
        }
    }
}

