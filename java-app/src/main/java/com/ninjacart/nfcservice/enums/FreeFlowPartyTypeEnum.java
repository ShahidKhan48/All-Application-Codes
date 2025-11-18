package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum FreeFlowPartyTypeEnum {
  BUYER(1, "Buyer"),
  SELLER(2, "Seller"),
  CR(3, "CR"),
  ADMIN(4, "Admin");

  int id;
  String value;
}


