export interface IAgentAppEndMyDayReport{
  'agent-cash-collected-amount': {
    collection_grouped_values: {
      cash: string; // e.g. "0.00"
    };
    deposit_slip_details: any[]; // can be typed properly if structure known
    collection_details: CollectionDetail[];
    cash_collected_amount: number;
  };
  outstanding_amount: {
    store_outstanding_amount_list: StoreOutstandingAmount[];
    total_outstanding_amount: string; // e.g. "1800.00"
  };
}

export interface CollectionDetail {
  store_id: number;
  amount: number;
  payment_mode: string; // "NEFT", "CASH", etc.
  comments: string;
  cash_transaction_status: string; // e.g. "INIT"
  image_url: string;
  approval_status: string; // "PENDING", "APPROVED", etc.
  bank_reference_no: string;
  cash_transaction_id: number;
  name: string;
  rejection_reason: string | null;
  validation_status: string; // e.g. "INVALID"
  validation_details: string; // JSON string
}

export interface StoreOutstandingAmount {
  store_id: number;
  outstanding_amount: string; // e.g. "1800.00"
  store_name: string;
}
