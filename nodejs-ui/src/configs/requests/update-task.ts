import { apiClient } from "../api-client";
import { paths } from "../paths";

export const agentTaskUpdateAsync = async (payload: {
  taskId: number;
  setStartTime: true;
  location: { lat: string; lon: string };
}) => {
  try {
    const response = await apiClient.post(paths.agentTaskUpdate(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};
