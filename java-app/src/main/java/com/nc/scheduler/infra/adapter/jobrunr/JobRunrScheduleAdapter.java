package com.nc.scheduler.infra.adapter.jobrunr;

import com.nc.scheduler.core.domain.exception.WorkflowAlreadyExistsException;
import com.nc.scheduler.core.domain.exception.WorkflowNotFoundException;
import com.nc.scheduler.core.domain.port.out.WorkflowExecutionPort;
import com.nc.scheduler.core.domain.port.out.WorkflowSchedulingPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.RecurringJobBuilder;
import org.jobrunr.storage.RecurringJobsResult;
import org.jobrunr.storage.StorageProvider;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation of WorkflowSchedulingPort using JobRunr.
 * This adapter only handles JobRunr-specific scheduling operations.
 * Business logic (endpoint building, validation, etc.) is handled by the service layer.
 * 
 * @author Ninja Workflow
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JobRunrScheduleAdapter implements WorkflowSchedulingPort {
    
    private final WorkflowExecutionPort workflowExecutionPort;
    private final JobScheduler jobScheduler;
    private final StorageProvider storageProvider;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String scheduleRecurring(String jobName, String endpoint, String payload, String cronExpression) {
        try {

            jobScheduler.deleteRecurringJob(jobName);

            // Register a new recurring job using JobRunr
            String jobId = jobScheduler.scheduleRecurrently(
                    jobName,
                    cronExpression,
                    () -> workflowExecutionPort.execute(endpoint, payload)
            );
            
            log.debug("Successfully scheduled recurring job '{}' with ID: {}", jobName, jobId);
            return jobId;
            
        } catch (WorkflowAlreadyExistsException e) {
            throw e; 
        } catch (Exception e) {
            log.error("Failed to schedule recurring job '{}'", jobName, e);
            throw new RuntimeException("Failed to schedule recurring job: " + jobName, e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRecurring(String jobName) {
        try {
            
            // Check if recurring job exists before deleting
            Instant scheduledInstant = storageProvider.getRecurringJobLatestScheduledInstant(jobName);

            if (scheduledInstant == null) {
                log.warn("Workflow '{}' not found for update", jobName);
                throw new WorkflowNotFoundException(jobName);
            }
            
            jobScheduler.deleteRecurringJob(jobName);
            
        } catch (WorkflowNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete recurring job '{}'", jobName, e);
            throw new RuntimeException("Failed to delete recurring job: " + jobName, e);
        }
    }
}
