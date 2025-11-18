package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.Address;
import com.ninjacart.nfcservice.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitiatedByDto {

    private String name;
    @NotNull
    @JsonProperty("app_id")
    private String appId;
    private String phone;
    private String email;
    private Address address;
    @NotEmpty
    @JsonProperty("user_id")
    private String userId;
    private RoleEnum role;
    @NotNull
    @JsonProperty("nfc_user_id")
    private Integer nfcUserId;
    @JsonProperty("realm_id")
    private String realmId;
}
