import { apiClient } from '../api-client';
import { paths } from '../paths';

export const agentTaskUpdateAsync = async (payload: {
  taskId: number;
  setStartTime: boolean;
  location: {
    lat: string;
    lon: string;
  };
}) => {
  const response = await apiClient.post(paths.agentTaskUpdate(), payload);
  return response.data;
};

export const cancelAgentTaskAsync = async (taskId: string) => {
  const response = await apiClient.post(paths.cancelAgentTask(), {
    input: {
      task_id: taskId,
    },
  });
  return response.data;
};

export const getEmandateStatusAsync = async (
  mandate_id: string,
  store_id: string,
  realm_id: string
) => {
  const response = await apiClient.post(
    paths.getEmandateStatus(store_id, realm_id),
    {
      input: {
        mandate_id: mandate_id,
      },
    },
    {
      headers: {
        'x-nc-system-user-id': store_id,
      },
    }
  );
  return response.data;
};
