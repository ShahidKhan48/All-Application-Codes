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
public class MatrixUserAuthResponseDto {

    @JsonProperty("nfc_user_id")
    private Integer nfcUserId;

    @JsonProperty("provider_id")
    private String providerId;

    @JsonProperty("provider_user_id")
    private String providerUserId;

    @JsonProperty("chat_user_id")
    private String chatUserId;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("access_token")
    private String token;

}
