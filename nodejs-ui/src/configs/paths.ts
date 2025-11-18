import { getUserIds } from "@/lib/storage";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const WORKFLOW_URL = `${API_BASE_URL}/workflow-engine`;
const NFC_API_URL = `${import.meta.env.VITE_PUBLIC_NFC_API_URL}`;
const DAM_API_URL = `${API_BASE_URL}/dam/api`;

export const getWorkflowURL = (
  withAuth = true,
  realmId = getUserIds()?.realmIdentifier,
  userId = getUserIds()?.userId
) => {
  if (withAuth === false) {
    return `${WORKFLOW_URL}/1/1/v1/execution/service/runWithNoAuth`;
  } else {
    return `${WORKFLOW_URL}/${realmId}/${userId}/v1/execution/service/run`;
  }
};
export const paths = {
  sendOTP: () => `${getWorkflowURL(false)}/user-login-send-otp`,
  verifyOTP: () => `${getWorkflowURL(false)}/omni-channel-login-verify-otp`,
  dashboardApi: () => `${getWorkflowURL(true)}/agent-app-homepage`,
  updateAgentLocationPath: () =>
    `${getWorkflowURL(true)}/update-agent-location`,
  closeAgentTask: () => `${getWorkflowURL(true)}/close-agent-task`,
  fetchUserRolesAndPrivileges: (realmId, userId) =>
    `${getWorkflowURL(true, realmId, userId)}/fetch-user-roles-and-privileges`,
  fetchCollectionById: () =>
    `${getWorkflowURL(true)}/fetch-collections-by-task-id`,
  fetchStoreD2cOutstanding: () =>
    `${getWorkflowURL(true)}/fetch-store-d2c-outstanding-amount`,
  runEnachMandate: () => `${getWorkflowURL(true)}/enach-mandate`,
  searchInventoryUserByAcess: () =>
    `${getWorkflowURL(true)}/search-business-relations-by-access`,
  fetchInventorySellerCatlouge: () =>
    `${getWorkflowURL(true)}/fetch-inventory-seller-catalogs`,
  fetchSellerProductCatalogs: () =>
    `${getWorkflowURL(true)}/fetch-seller-product-catalogs`,
  createWholeSaleOrder: () => `${getWorkflowURL(true)}/create-wholesale-order`,
  initiateAgentApprovalFlow: () =>
    `${getWorkflowURL(true)}/initiate-agent-payment-approval-flow`,
  searchInventoryUser: () => `${getWorkflowURL(true)}/search-business-relations-by-access`,
  agentOrderListing: () => `${getWorkflowURL(true)}/agent-order-listing`,
  deleteCashTxn: () => `${getWorkflowURL(true)}/delete-cash-transaction`,
  updateCashTxn: () =>
    `${getWorkflowURL(
      true
    )}/delete-and-create-new-cash-txn-for-d2c-approval-flow`,
  uplodeFileRequest: () =>
    `${WORKFLOW_URL}/${getUserIds()?.realmIdentifier}/${
      getUserIds()?.userId
    }/v1/uploadFile/request`,
  getInventryOrderDetails: () =>
    `${getWorkflowURL(true)}/get-inventory-order-details`,
  updateWholesaleOrderQnty: () =>
    `${getWorkflowURL(true)}/update-wholesale-order-status-with-qty`,
  fetchStoreOutstandingAmt: () =>
    `${getWorkflowURL(
      true
    )}/fetch-stores-outstanding-amount-and-cash-collected-amount`,
  agentAppEndMyDay: () => `${getWorkflowURL(true)}/agent-app-end-day`,
  createAgentTask: () => `${getWorkflowURL()}/create-agent-task`,
  fetchAgentFacility: () => `${getWorkflowURL()}/fetch-agent-facility`,
  agentAppStartDay: () => `${getWorkflowURL()}/agent-app-start-day`,
  agentTaskUpdate: () => `${getWorkflowURL()}/agent-task-update`,
  createDepositSlipParentCashTxn: () =>
    `${getWorkflowURL()}/create-deposit-slip-parent-cash-txn`,
  rejectCashTxn: () => `${getWorkflowURL()}/reject-cash-txn`,
  cancelAgentTask: () => `${getWorkflowURL()}/cancel-agent-task`,
  getEmandateStatus: (store_id : string, realm_id : string) => `${getWorkflowURL(true,realm_id,store_id)}/get-mandate-status`,
  fetchCashTxnStatus: () => `${getWorkflowURL()}/fetch-cash-txn-status`,
};
