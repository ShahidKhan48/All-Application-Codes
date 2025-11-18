export interface Descriptor {
  name: string;
  shortDesc: string;
  code: string;
  symbol: string;
  media: MediaDetails;
  longDesc: string;
}

export interface AdditionalDetails {
  refType: string;
  refValue: string;
}
export interface SellerInfo {
  name: string;
  latitude: string;
  sellerId: string;
  longitude: string;
}
export interface CancellationTermsEntity {
  refundEligible: boolean;
  cancellationFee: CancellationFee;
  cancelBy: string;
  fulfillmentState: string;
}
export interface CancellationFee {
  percentage?: number | null;
  amount?: Price | null;
}

export interface ReturnTermsEntity {
  returnEligible: boolean;
  returnWithin: string;
  fulfillmentManagedBy: string;
}
export interface Manufacturer {
  contact: Contact;
  descriptor: Descriptor;
  address: Address;
}
export interface Contact {
  contactNumber?: string[] | null;
  address?: Address[] | null;
  alternateNumber?: string[] | null;
  website?: string;
  email?: string[] | null;
}

export interface Address {
  plot: string;
  landmark: string;
  village: string;
  district: string;
  state: string;
  pincode: string;
  street: string;
  locality: string;
  latitude: string;
}

export type MediaDetails = {
  id?: string;
  type: string;
  visibility: string;
  media: MediaDetail[];
};

export type MediaDetail = {
  id?: string;
  location?: string;
  mediaUrl: string;
  fileName: string;
  mediaType: string;
  visibility?: string;
  mediaFormat?: string;
  type?: any;
};

export interface CatalogItemRes {
  ProductCatalog: ProductCatalog;
}

export interface InventoryItemRes {
  id: Number;
  from_user_id: Number;
  to_user_id: Number;
  owner_id: Number;
  transaction_date: string;
  transaction_type: string;
  reference_id: string;
  reference_type: string;
  product_id: string;
  product_name: string;
  quantity: Number;
  description: string;
}
export interface ProductCatalog {
  realmId: string;
  userId: string;
  id?: string;
  brand?: string;
  parentId: string;
  descriptor: Descriptor;
  price: Price;
  active: boolean;
  quantity: Quantity;
  reservedQuantity: Quantity;
  catalogType: string;
  userInfo: UserInfo;
  providerToolId: string;
  modifiedAt: string;
  subCategoryName: string;
  categoryName: string;
  mrp: MRP;
  taxes: any[];
  uom: MeasurementUnit;
}

export interface Price {
  minimumPrice: number;
  maximumPrice: number;
  currency: string;
  measurementUnit: MeasurementUnit;
}
export interface MeasurementUnit {
  base: string;
  value: number;
}
export interface MRP {
  currency: string;
  value: number;
}
export interface Quantity {
  value: number;
  measurementUnit: MeasurementUnit;
  packing: string;
}
export interface UserInfo {
  name: string;
  contact: Contact;
}
