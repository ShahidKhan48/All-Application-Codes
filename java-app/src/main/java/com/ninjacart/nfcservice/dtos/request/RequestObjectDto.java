package com.ninjacart.nfcservice.dtos.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.ApprovalsDto;
import com.ninjacart.nfcservice.dtos.FreeFlowPartyDto;
import com.ninjacart.nfcservice.dtos.InitiatedByDto;
import com.ninjacart.nfcservice.dtos.UserRoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestObjectDto {

    @NotNull
    @Valid
    private SellerDto seller;
    @NotNull
    @Valid
    private SellerDto buyer;
    @NotNull
    @JsonProperty("request_type")
    private Integer requestType;
    @JsonProperty("chat_room_id")
    private String chatRoomId;
    @JsonProperty("request_group_id")
    private String requestGroupId;
    @NotNull
    @Valid
    private List<ItemsDto> items;
    private String message;
    private Integer ownerId;
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    @JsonProperty("user_roles")
    private List<UserRoleDto> userRoleDtos;

    @JsonProperty("entity_version")
    private Integer entityVersion;
    @JsonProperty("entity_status")
    private String entityStatus;
    private Integer id;
    private List<ApprovalsDto> approvals;
    @JsonProperty("copy_id_to_external_reference_id")
    private boolean copyIdToExternalReferenceId;
    private Integer orderId;
    private Integer status;
    private Boolean active;
    @JsonProperty("delivery_date")
    private String deliveryDate;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("admin_info")
    private InitiatedByDto adminInfo;
    @JsonProperty("admin_assisted_flow")
    private boolean adminAssistedFlow;
    @JsonProperty("payment_terms")
    private String paymentTerms;
    @JsonProperty("quality_terms")
    private String qualityTerms;
    private List<UserRoleDto> customerRepresentativeDto;
    private List<FreeFlowPartyDto> freeFlowPartyDtos;


}
