package com.nc.scheduler.core.domain.util;

/**
 * Enumeration representing the status of API responses.
 * 
 * @author Ninja Workflow
 */
public enum ResponseStatus {
    
    /**
     * Operation completed successfully - scheduled.
     */
    SCHEDULED("scheduled"),
    
    /**
     * Operation completed successfully - updated.
     */
    UPDATED("updated"),
    
    /**
     * Operation completed successfully - deleted.
     */
    DELETED("deleted"),
    
    /**
     * Operation completed successfully - enqueued.
     */
    ENQUEUED("enqueued"),
    
    /**
     * Operation failed with an error.
     */
    ERROR("error");
    
    private final String value;
    
    ResponseStatus(String value) {
        this.value = value;
    }
    
    /**
     * Returns the string value of the status.
     * 
     * @return the status value
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}

