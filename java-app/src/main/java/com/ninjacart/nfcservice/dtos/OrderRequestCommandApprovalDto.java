package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.Address;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequestCommandApprovalDto {

    @JsonProperty("request_type")
    private String requestType;
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("delivery_date")
    private String deliveryDate;
    @JsonProperty("reference_id")
    private String referenceId;
    private List<ItemsDto> items;
    private List<ApprovalsDto> approvals;
    private List<UserRoleDto> userRoles;


}
