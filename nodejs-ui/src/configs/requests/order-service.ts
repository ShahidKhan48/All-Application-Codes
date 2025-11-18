import { apiClient } from "../api-client";
import { paths } from "../paths";

interface FetchOrderListRequestProps {
  input: { from_date: string; user_id: string; page: number; size: number };
}

export const fetchAgentOrderListingAsync = async (
  payload: FetchOrderListRequestProps
) => {
  const response = await apiClient.post(paths.agentOrderListing(), payload);
  return response;
};

export const getInventryOrderDetailsAsync = async (order_id: number) => {
  const response = await apiClient.post(paths.getInventryOrderDetails(), {
    input: { order_id: order_id },
  });
  return response.data;
};

export const updateWholesaleOrderQntyAsync = async (
  id,
  entity_status,
  items,
  cancellation_reason
) => {
  const requestBody = {
    input: {
      id,
      entity_status,
      items,
      ...(cancellation_reason && { cancellation_reason }),
    },
  };
  const response = await apiClient.post(
    paths.updateWholesaleOrderQnty(),
    requestBody
  );
  return response.data;
};

export const fetchStoreOutstandingAmtAsync = async (payload: {
  deposit_slip_task_id: string;
  user_id: string;
  task_date: string;
}) => {
  const requestBody = {
    input: payload,
  };
  const response = await apiClient.post(
    paths.fetchStoreOutstandingAmt(),
    requestBody
  );
  return response.data;
};

export const agentAppEndMyDay = async (payload: {
  request: {
    task_id: string;
    deposit_slip: boolean;
    deposit_amount: number;
    additional_info: any[];
  };
}) => {
  const response = await apiClient.post(paths.agentAppEndMyDay(), payload);
  return response.data;
};
