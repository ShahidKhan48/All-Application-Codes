package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderRequestCommandStatusEnum {
    PENDING,
    APPROVED,
    REJECTED;

}
