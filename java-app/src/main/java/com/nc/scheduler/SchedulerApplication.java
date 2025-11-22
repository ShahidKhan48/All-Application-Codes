package com.nc.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Ninja Workflow Scheduler.
 * This service provides job scheduling capabilities using JobRunr with MongoDB storage.
 * 
 * @author Ninja Workflow
 */
@SpringBootApplication
public class SchedulerApplication {

    /**
     * Main entry point for the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}

