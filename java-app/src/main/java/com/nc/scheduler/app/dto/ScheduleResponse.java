package com.nc.scheduler.app.dto;

import com.nc.scheduler.core.domain.util.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for schedule operation responses.
 * 
 * @author Ninja Workflow
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponse {
    
    /**
     * Status of the operation.
     */
    private ResponseStatus status;
    
    /**
     * Unique identifier of the scheduled job.
     */
    private String jobId;
    
    /**
     * Name of the job.
     */
    private String jobName;
    
    /**
     * Optional message providing additional information.
     */
    private String message;
}

