export interface IDashboardData {
  deposit_slip_task_id: number;
  day_task_id: number;
  disable_payment_collections: boolean;
  agent_task_response: AgentTaskResponse[];
  geo_fence_distance_for_delivery: number;
  task_date: string;
  enable_end_day_button: boolean;
  enable_upload_deposit_slip: boolean;
  enable_start_day_button: boolean;
  geo_fence_distance: number;
  comment: string;
  deposit_slip_comment: string;
  deposit_slip_completed: boolean;
}

export interface AgentTaskResponse {
  distance?: number | null;
  priority: number;
  lat: number;
  lng: number;
  address?: string | null;
  tasks: Task[];
  store_id: number;
  store_name: string;
  store_realm_id: string;
  store_status: string;
  image_url?: string | null;
  contact_number: string;
  store_collection_rejected: boolean;
  store_contact_numbers: string[];
  business_unit: string;
  business_subunit: string;
}

export interface Task {
  id: number;
  status: "IN_PROGRESS" | "COMPLETED" | "CANCELLED" | "PENDING";
  user_id: number;
  store_id: number;
  ref_id?: number | null;
  order_id?: number | null;
  warehouse_id?: number | null;
  task_type:
    | "STORE_VISIT"
    | "STORE_AUDIT"
    | "DELIVERY"
    | "DELIVERY_VERIFICATION"
    | "DUE_COLLECTION";
  task_date: string;
  priority?: number | null;
  expected_start_time?: string | null;
  expected_end_time?: string | null;
  travel_km?: number | null;
  total_travel_km?: number | null;
  travel_time?: number | null;
  additional_info?: AdditionalInfo | null;
  start_time?: string | null;
  end_time?: string | null;
  enable_close_button: boolean;
  enable_cancel_button: boolean;
  created_at: string;
}

export interface AdditionalInfo {
  location?: {
    lon: number;
    lat: number;
  };
  comments?: string;
  images: string[];
  reason?: string;
}

export const TaskTypeEnum = Object.freeze({
  DELIVERY: "DELIVERY",
  DUE_COLLECTION: "DUE_COLLECTION",
  STORE_AUDIT: "STORE_AUDIT",
  STORE_VISIT: "STORE_VISIT",
  DELIVERY_VERIFICATION: "DELIVERY_VERIFICATION",
  ENACH: "ENACH",
});

// TaskStatusEnum equivalent
export const TaskStatusEnum = Object.freeze({
  PENDING: "PENDING",
  IN_PROGRESS: "IN_PROGRESS",
  COMPLETED: "COMPLETED",
  CLOSE: "CLOSE",
  CANCELLED: "CANCELLED",
  DISPATCHED: "DISPATCHED",
});

export interface Product {
  id: string;
  descriptor: {
    name: string;
    shortDesc: string;
    longDesc: string;
    media: {
      type: string;
      media: {
        mediaUrl: string;
        location: string;
        id: string;
        mediaFormat: string;
      }[];
    };
  };
  price: {
    currency: string;
    value: number;
    minimumPrice: number;
    maximumPrice: number;
  };
  catalogType: string;
  quantity: {
    value: number;
  };
  actualQuantity: {
    value: number;
  };
  packingCharges: {
    value: number;
    currency: string;
  };
  active: boolean;
  realmId: string;
  userId: string;
  providerToolId: string;
  parentId: string;
  subCategoryName: string;
  categoryName: string;
  taxes: {
    type: string;
    value: number;
  }[];
  mrp: {
    currency: string;
    value: number;
  };
  uom: {
    base: string;
    value: number;
  };
  brand: string;
  tags: string[];
  hsnCode: string;
  fssaiLicense: string;
  eanCode: string;
}

export interface CollectionResponse {
  amount_collected_in_cash: number;
  total_cash_collection: number;
  collection_details: CollectionDetail[];
  all: DocumentDetail[];
  kyc_status: boolean;
}

export interface CollectionDetail {
  amount: number;
  payment_mode: string; // e.g. "CASH" | "CHEQUE"
  comments: string;
  image_url: string;
  approval_status: string; // e.g. "PENDING"
  bank_reference_no: string;
  cash_transaction_id: number;
  payment_link: string | null;
  qr_link: string | null;
  transaction_creation_date: string; // ISO datetime string
  expiry_by: string | null;
  name: string;
  rejection_reason: string | null;
  status: string; // e.g. "INIT"
}

export interface DocumentDetail {
  id: string;
  realmId: string;
  userId: string;
  name: string; // e.g. "PAN", "GST"
  validFrom: string | null;
  validTo: string | null;
  expiry_ts: string | null;
  status: string; // e.g. "PENDING"
  externalReferenceId: string | null;
  tags: any[]; // can refine if structure known
  comment: string;
  rejectionDetails: any[]; // refine if structure known
  digitalAssetDefinitionId: string;
  mediaDetails: any[]; // refine if structure known
  verificationDetail: VerificationDetail;
  documentData: any | null;
  collectionRequestId: string;
  actions: Action[];
  payload: any | null;
  metadata: any[];
  submittedBy: string | null;
  submittedAt: string | null;
  createdAt: string; // ISO datetime
  updatedAt: string; // ISO datetime
  deleted: boolean;
}

export interface VerificationDetail {
  provider: string | null;
  verified_ts: string | null;
}

export interface Action {
  name: string; // e.g. "PAN_VALIDATE", "upload"
  type: string | null;
  initiatorType: string; // e.g. "USER"
  operation: string; // e.g. "validate" | "upload"
  mode: string; // e.g. "PROCESS"
  inputType: string; // e.g. "TEXT" | "FILE"
  provider: string; // e.g. "karza" | "ninjacart"
  event: string | null;
  status: string | null;
  statusComment: string | null;
  disableStatusMigration: boolean | null;
  digitalAssetUpdateConfig: any | null;
}
