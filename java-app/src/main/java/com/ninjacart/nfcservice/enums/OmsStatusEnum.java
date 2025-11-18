package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.naming.ldap.PagedResultsControl;

@AllArgsConstructor
@Getter
public enum OmsStatusEnum {

    APPROVED(20),
    REJECTED(21);

    final int type;
}
