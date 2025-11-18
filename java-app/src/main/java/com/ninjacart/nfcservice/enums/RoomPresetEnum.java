package com.ninjacart.nfcservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RoomPresetEnum {
    PRIVATE_CHAT("private_chat"),
    PUBLIC_CHAT("public_chat"),
    TRUSTED_PRIVATE_CHAT("trusted_private_chat");

    final String type;
}
