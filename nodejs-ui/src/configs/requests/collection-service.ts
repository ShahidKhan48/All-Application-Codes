import { getUserIds } from "@/lib/storage";
import { apiClient } from "../api-client";
import { paths } from "../paths";

export interface ICollection {
  amount: number;
  payment_mode: "CASH" | "CARD" | "UPI" | "NETBANKING" | string; // extend as needed
  comments: string;
  image_url: string;
  approval_status: "PENDING" | "APPROVED" | "REJECTED" | string;
  bank_reference_no: string;
  cash_transaction_id: number;
  payment_link: string | null;
  qr_link: string | null;
  transaction_creation_date: string; // ISO date string
  expiry_by: string | null; // could be string or null
  name: string;
  rejection_reason: string | null;
  status: "INIT" | "SUCCESS" | "FAILED" | string;
}

export interface InitiateAgentPaymentApprovalFlowRequest {
  input: {
    store_user_id: string;
    task_id: string;
    amount: number;
    store: {
      user_id: string;
      name: string;
      realm_id: string;
      contact_number: string;
      business_sub_unit: string;
    };
    payment_type: "CASH" | "CHEQUE" | "UPI" | "ONLINE" | string; // extend as per backend
    utr_number: string;
    transaction_id: string;
    comments: string;
    image_url: string;
  };
}

export const fetchCollectionByIdAsync = async (payload: {
  input: { task_id: number; store_id: string; fetch_kyc_info: boolean };
}) => {
  const response = await apiClient.post(paths.fetchCollectionById(), payload);
  return response?.data;
};

export const fetchStoreD2cOutstandingAsync = async (payload: {
  input: {
    store_user_id: number;
    fetch_order_info: boolean;
    warehouse_user_ids: string[];
  };
}) => {
  const response = await apiClient.post(
    paths.fetchStoreD2cOutstanding(),
    payload
  );
  return response?.data;
};

export const runEnachMandateAsync = async (payload: {
  input: {
    userName: string;
    mobile: string;
    referenceId: string;
    taskId: string;
    expiresOn: string;
    enach_max_amount: string;
  };
}) => {
  const response = await apiClient.post(paths.runEnachMandate(), payload);
  return response?.data;
};

export const searchInventoryUserByAcessAsync = async (payload: {
  input: {
    businessRelationsFilter: {
      userTypes: string[];
      searchTerm: string;
      pageNo: number;
      pageSize: number;
    };
    fetchRequestType: string;
  };
}) => {
  const response = await apiClient.post(
    paths.searchInventoryUserByAcess(),
    payload
  );
  return response?.data;
};

export const fetchInventorySellerCatlougeAsync = async (
  businessUnit: string,
  userId: string
) => {
  const response = await apiClient.post(paths.fetchInventorySellerCatlouge(), {
    input: {
      businessUnit: businessUnit,
      loggedInUserType: "AGENT",
      loggedInUserId: getUserIds()?.userId,
      catalogFilters: [
        {
          from: 0,
          size: 5000,
          active: true,
          userId: userId,
          providerToolId: ["NINJA_INVENTORY"],
        },
      ],
    },
  });
  return response?.data;
};

export const fetchSellerProductCatalogsAsync = async (userId: string) => {
  const requestBody = {
    input: {
      from: 0,
      size: 5000,
      active: true,
      userId: userId,
      providerToolId: ["NINJA_INVENTORY"],
      parentRealmId: getUserIds()?.realmIdentifier,
    },
  };
  const response = await apiClient.post(
    paths.fetchSellerProductCatalogs(),
    requestBody
  );
  return response?.data;
};

export const createWholeSaleOrderAsync = async (orderPayload) => {
  const response = await apiClient.post(
    paths.createWholeSaleOrder(),
    orderPayload
  );
  return response?.data;
};

export const initiateAgentApprovalFlowAsync = async (
  payload: InitiateAgentPaymentApprovalFlowRequest
) => {
  const response = await apiClient.post(
    paths.initiateAgentApprovalFlow(),
    payload
  );
  return response?.data;
};

export const searchInventoryUserAsync = async (payload: {
  input: {
    businessRelationsFilter: {
      pageNo: number;
      pageSize: number;
      userTypes: string[];
      searchTerm: string;
    };
    fetchRequestType : string
  };
}) => {
  const response = await apiClient.post(paths.searchInventoryUser(), payload);
  return response?.data;
};

export const deleteCashTxnAsync = async (payload: {
  input: {
    cashTransactionIds: string[];
    destinations: [
      {
        destination: string;
      }
    ];
    amount: string;
    store_name: string;
  };
}) => {
  const response = await apiClient.post(paths.deleteCashTxn(), payload);
  return response?.data;
};

export const updateCashTxnAsync = async (payload: {
  input: {
    existing_cash_txn_id: string;
    destinations: [
      {
        destination: string;
      }
    ];
    amount: string;
    store_name: string;
    payment_type: string;
    utr_number: string;
    comments: string;
    image_url: string;
    old_amount: string;
  };
}) => {
  const response = await apiClient.post(paths.updateCashTxn(), payload);
  return response?.data;
};

export const uplodeFileRequest = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  const response = await apiClient.post(paths.uplodeFileRequest(), formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return response.data;
};

export const fetchCashTxnStatusAsync = async (id) => {
  const response = await apiClient.post(paths.fetchCashTxnStatus(), {
    input: {
      cash_transaction_id: id,
    },
  });
  return response?.data;
};
