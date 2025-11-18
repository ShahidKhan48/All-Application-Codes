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
public class OnboardResponseDto {

    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("home_server")
    private String homeServer;
    @JsonProperty("device_id")
    private String deviceId;
}
