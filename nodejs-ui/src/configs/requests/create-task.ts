import { apiClient } from "../api-client";
import { paths } from "../paths";

interface CreateAgentTaskRequestProps {
  input: {
    agent_user_id: string;
    store_id: number;
    task_type: string;
    task_date: string;
    status: string;
  }[];
}

interface FetchAgentFacilityRequestProps {
  input: {
    statuses: string[];
    agentIds: string[];
    agentTypes: string[];
    facilityIds: any[];
    facilityTypes: string[];
    page: number;
    size: number;
  };
}

export const createAgentTaskAsync = async (
  payload: CreateAgentTaskRequestProps
) => {
  try {
    const response = await apiClient.post(paths.createAgentTask(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};

export const fetchAgentFacilityAsync = async (
  payload: FetchAgentFacilityRequestProps
) => {
  try {
    const response = await apiClient.post(paths.fetchAgentFacility(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};
