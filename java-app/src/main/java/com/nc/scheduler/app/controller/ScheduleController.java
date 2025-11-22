package com.nc.scheduler.app.controller;

import com.nc.scheduler.app.dto.*;
import com.nc.scheduler.core.domain.port.in.WorkflowSchedulePort;
import com.nc.scheduler.core.domain.util.ResponseStatus;
import com.nc.scheduler.core.domain.util.SchedulerUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing workflow schedules.
 * Provides endpoints for scheduling, updating, and managing workflow schedules.
 * 
 * @author Ninja Workflow
 */
@Slf4j
@RestController
@RequestMapping("/api/realms/{realmId}")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final WorkflowSchedulePort workflowSchedulePort;
    private final ObjectMapper objectMapper;
    
    /**
     * Schedules a recurring workflow execution.
     * 
     * @param request the schedule request containing workflow details
     * @return response with job ID and status
     */
    @PostMapping("/workflows/recurring")
    public ResponseEntity<ScheduleResponse> scheduleRecurringWorkflow(
            @PathVariable String realmId,
            @Valid @RequestBody ScheduleRecurringRequest request) 
            throws JsonProcessingException {
        log.debug("Received workflow recurring schedule request: workflowName='{}', cron='{}'", 
                request.getJobName(), request.getCronExpression());
        
        String payloadJson = objectMapper.writeValueAsString(request.getPayload());
        String jobName = SchedulerUtils.buildJobName(realmId, request.getJobName());
        String jobId = workflowSchedulePort.scheduleRecurringWorkflow(
                realmId,
                request.getJobName(),
                payloadJson,
                request.getCronExpression()
        );
        
        return ResponseEntity.ok(new ScheduleResponse(
                ResponseStatus.SCHEDULED,
                jobId,
                jobName,
                "Recurring workflow scheduled successfully"
        ));
    }
    
    /**
     * Deletes a recurring workflow.
     * 
     * @param workflowName the name of the workflow to delete
     * @return response with status
     */
    @DeleteMapping("/workflows/recurring/{workflowName}")
    public ResponseEntity<ScheduleResponse> deleteRecurringWorkflow(
            @PathVariable String realmId,
            @PathVariable String workflowName) {
        log.debug("Received delete request for recurring workflow: '{}'", workflowName);
        
        workflowSchedulePort.deleteRecurringWorkflow(realmId, workflowName);
        String jobName = SchedulerUtils.buildJobName(realmId, workflowName);
        
        return ResponseEntity.ok(new ScheduleResponse(
                ResponseStatus.DELETED,
                null,
                jobName,
                "Recurring workflow deleted successfully"
        ));
    }
}
