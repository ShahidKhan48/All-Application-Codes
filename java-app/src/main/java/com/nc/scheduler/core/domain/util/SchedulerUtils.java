package com.nc.scheduler.core.domain.util;

import lombok.experimental.UtilityClass;

/**
 * Utility methods for building workflow-related identifiers.
 */
@UtilityClass
public class SchedulerUtils {

    public static final String IDENTIFIER_DELIMITER = "-";

    /**
     * Builds a qualified job name using the realm identifier and workflow name.
     *
     * @param realmId     realm identifier
     * @param workflowName workflow name
     * @return qualified job name in the form {@code realmId-workflowName}
     */
    public String buildJobName(String realmId, String workflowName) {
        return realmId + IDENTIFIER_DELIMITER + workflowName;
    }
}


