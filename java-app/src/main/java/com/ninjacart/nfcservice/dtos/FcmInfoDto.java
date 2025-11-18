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
public class FcmInfoDto {
    @JsonProperty("fcm_id")
    String fcmId;
    @JsonProperty("provider_user_id")
    String providerUserId;
    @JsonProperty("provider_id")
    String providerId;

}
