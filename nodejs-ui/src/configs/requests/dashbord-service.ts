import { apiClient } from "../api-client";
import { paths } from "../paths";

interface TaskRequestPayload {
  request: {
    start_location: string; // "lat,long" format
    task_date: string;      // ISO date or YYYY-MM-DD format
  };
}

interface AgentLocationInput {
  input: {
    agentId: string;      // "1713027"
    date: string;         // "2025-06-30" (YYYY-MM-DD format)
    latitude: string;     // "37.4219983"
    longitude: string;    // "-122.084"
    timestamp: number;    // 1755081465620 (epoch ms)
  };
}

interface TaskUpdatePayload {
  input: {
    task_id: string; 
    additional_info: Array<{
      comments?: string;
      images?: string[];
      reason?: string;
    }>;
  };
}


export const dashboardApiAsync = async (payload: TaskRequestPayload) => {
  const response = await apiClient.post(paths.dashboardApi(), payload);
  return response?.data;
};

export const updateAgentLocationAsync = async (payload: AgentLocationInput) => {
  const response = await apiClient.post(paths.updateAgentLocationPath(), payload);
  return response?.data;
};

export const closeAgentTaskAsync = async (payload: TaskUpdatePayload) => {
  const response = await apiClient.post(paths.closeAgentTask(), payload);
  return response?.data;
};

