package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.Address;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderRequestMessageDto {

    private Integer requestType;
    private String userName;
    private Address address;
    private List<ItemsDto> items;
    private String userMessage;
    private List<UserRoleDto> userRoles;
    private SellerDto buyer;
    private SellerDto seller;
    private List<ApprovalsDto> approvals;
    @JsonProperty("entity_version")
    private Integer entityVersion;
    @JsonProperty("entity_status")
    private String entityStatus;
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    private Integer id;
    @JsonProperty("delivery_date")
    private String deliveryDate;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("initiated_by")
    private InitiatedByDto initiatedByDto;
    @JsonProperty("admin_assisted_flow")
    private boolean adminAssistedFlow;
    @JsonProperty("payment_terms")
    private String paymentTerms;
    @JsonProperty("quality_terms")
    private String qualityTerms;
    private String orderId;

}
