import { apiClient } from "../api-client";
import { paths } from "../paths";

export const agentAppStartDayAsync = async (payload: {
  request: { start_location: string; task_date: string };
}) => {
  try {
    const response = await apiClient.post(paths.agentAppStartDay(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};

export const createDepositSlipParentCashTxnAsync = async (payload: {
  input: {
    transactions_list: {
      from: { user_id: number; user_name: string };
      to: { user_id: number; user_name: string };
      ref_id: string;
      amount: number;
      utr_number: string;
      image_url: string;
    }[];
  };
}) => {
  try {
    const response = await apiClient.post(
      paths.createDepositSlipParentCashTxn(),
      payload
    );
    return response?.data;
  } catch (e) {
    throw e;
  }
};

export const rejectCashTxnAsync = async (payload: {
  input: { cash_txn_id: number };
}) => {
  try {
    const response = await apiClient.post(paths.rejectCashTxn(), payload);
    return response?.data;
  } catch (e) {
    throw e;
  }
};
