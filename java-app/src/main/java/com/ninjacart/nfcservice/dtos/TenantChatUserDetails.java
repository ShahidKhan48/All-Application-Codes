package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TenantChatUserDetails {

    @JsonProperty("chat_user_id")
    private String chatUserId;
    @JsonProperty("chat_device_id")
    private String chatDeviceId;
    @JsonProperty("chat_token")
    private String chatToken;
    @JsonProperty("nfc_user_id")
    private String nfcUserId;
}
