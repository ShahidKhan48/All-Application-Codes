package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EntityStatusEnum {
    NEGOTIATION_PENDING,
    NEGOTIATION_APPROVED,
    NEGOTIATION_REJECTED;
}
