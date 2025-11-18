package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApprovalsDto {
    private String role;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("provider_id")
    private String providerId;
    @JsonProperty("disable_action")
    private boolean disableAction;
    private String status;
    @JsonProperty("nfc_user_id")
    private Integer nfcUserId;

}
