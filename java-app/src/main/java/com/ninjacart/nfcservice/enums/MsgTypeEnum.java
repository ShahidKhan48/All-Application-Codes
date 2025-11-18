package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MsgTypeEnum {
    COMMAND("m.command"),
    REPLACE("m.replace");

    final String type;
}
