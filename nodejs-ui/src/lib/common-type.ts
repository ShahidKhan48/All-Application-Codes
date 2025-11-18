//Ware House Invetory serac h 
export interface INVENTORY_WAREHOUSE_TYPE {
  Realm: Realm;
}

export interface Realm {
  commission: number;
  gstIn: string;
  id: string;
  additionalDetails: AdditionalDetail[];
  businessProfile: BusinessProfile;
  name: string;
  tags: string[];
  systemUserId: number;
  contactDetails: ContactDetails;
  parentRealm: string;
}

export interface AdditionalDetail {
  refType: string;   // e.g. "traderOnboardingStatus", "referralCode"
  refValue: string;  // corresponding value
}

export interface BusinessProfile {
  businessScoreVisibility: boolean;
  businessScoreAvailability: boolean;
  businessName: string;
  businessStartedYear: number;
  ratings: number;
  verificationStatus: boolean;
  businessScore: number;
}

export interface ContactDetails {
  primaryPhoneNumber: any;
  address: Address[];
  email: string[];
}

export interface Address {
  name: string; // e.g. "shipping", "billing"
  locality: string;
  city?: string; // optional because billing address did not have city
  state: string;
  location: Location;
  pincode: string;
}

export interface Location {
  lat: number;
  lon: number;
}



export interface IFetchStoreDTCAmt {
  rtv_orders: Order[]; // assuming always array
  total_etms_value: string;
  total_sale_amount: string;
  store_details: any | null;
  orders: Order[];
  outstanding_amount: string;
  credit_note_value: string;
}

export interface Order {
  amount_pending_for_approval: string;
  payload: Payload;
  derived_outstanding: string;
}

export interface Payload {
  createdAt: string; // ISO Date string
  totalAmount: number;
  entityDate: string; // ISO Date string
  outstandingAmount: number;
  freeFlowEntityPartyDTOList: FreeFlowEntityPartyDTO[];
  freeFlowEntityItemDTOList: FreeFlowEntityItemDTO[];
  id: number;
}

export interface FreeFlowEntityPartyDTO {
  freeFlowEntityId: number;
  deleted: boolean;
  name: string;
  id: number;
  partyType: number; // 1 = seller, 2 = buyer, 3 = other? (guessing from sample)
  gstIn?: string;
  userId: number;
  contactNumber?: string;
  email?: string;
}

export interface FreeFlowEntityItemDTO {
  quantity: number;
  cessPercentage?: number;
  igstPercentage?: number;
  orderId: number;
  gstPercentage?: number;
  subLineTotal: number;
  unitSellPrice: number;
  dispatchedQuantity?: number;
  version: number;
  itemId?: string;
  itemName: string;
  freeFlowEntityId: number;
  unitBuyPrice: number;
  deleted: boolean;
  tcsApplicable: boolean;
  imageURL?: string;
  additionalInfo?: string; // JSON stringified array
  id: number;
  itemTypeId: number;
  itemUomName?: string;
  status: number;
  coinCount: number;
}


export interface ISearchBusinessRelationsbyAccess {
  total: number | null;
  data: Relation[];
}

export interface Relation {
  id: number;
  parentRealmId: string | null;
  parentUserId: number | string | null;
  realmId: string | null;
  userId: number | string;
  userRealmId: string | null;
  name: string | null;
  contactNumber: string | null;
  businessName: string | null;
  email: string | null;
  gst: string | null;
  userType: string | null;
  commission: number | null;
  identityDetails: IdentityDetails | null;
  businessUnits: BusinessUnit[];
  payableAccountId: string | null;
  billingAddress: Address | null;
  shippingAddress: Address | null;
  alternateCommunications: AlternateCommunication[]; // currently empty array in sample
  totalOrders: number | null;
  lastOrderOn: string | null; // ISO date string or null
}

/* Business unit + subunits */
export interface BusinessUnit {
  businessUnit: string;
  businessSubUnits: string[]; // can contain ids or names
}

/* Address + location */
export interface Address {
  name: string | null;
  plot?: string | null;
  street?: string | null;
  landmark?: string | null;
  locality?: string | null;
  city?: string | null;
  state?: string | null;
  district?: string | null;
  village?: string | null;
  pincode?: string | null;
  location?: Location | null;
}

export interface Location {
  lat: number;
  lon: number;
}

/* Identity details (extend later if more identity types appear) */
export interface IdentityDetails {
  gst?: GSTIdentity | null;
  // add other identity types here when present
}

export interface GSTIdentity {
  gstNumber: string | null;
}

/* Placeholder for alternate communications (empty in sample) */
export type AlternateCommunication = any; // replace `any` with a stricter type if you know the shape

export interface IAddress {
  name: string;
  plot: string;
  street: string | null;
  landmark: string;
  locality: string;
  city: string;
  state: string;
  district: string;
  village: string;
  pincode: string;
  location: {
    lat: number;
    lon: number;
  };
}


