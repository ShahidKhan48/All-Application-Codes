package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProviderConfigEnum {
  CATEGORY,
  SUBCATEGORY,
  MANUAL,
  ROUNDROBIN,
  CITY,
  STATE,
  PINCODE,
  FACILITY;
}
