package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequestStatusCommandDto {

    private RoleEnum role;
    @JsonProperty("user_id")
    private String userId;
    private String requestType;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("delievery_date")
    private String deliveryDate;
    @JsonProperty("reference_id")
    private String referenceId;
}
