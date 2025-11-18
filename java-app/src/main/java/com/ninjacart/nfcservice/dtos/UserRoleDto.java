package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRoleDto {
    @JsonProperty("user_id")
    private String userId;
    private String role;
    @JsonProperty("provider_id")
    private String providerId;
    @JsonProperty("nfc_user_id")
    private Integer nfcUserId;
}
