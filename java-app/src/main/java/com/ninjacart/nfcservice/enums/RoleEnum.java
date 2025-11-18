package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoleEnum {
    BUYER,
    SELLER,
    BUYER_ADMIN,
    SELLER_ADMIN,
    SELLER_BUYER_ADMIN,
    ADMIN;
}
