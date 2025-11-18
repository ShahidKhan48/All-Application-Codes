package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.ninjacart.nfcservice.dtos.request.Address;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderManagementRequestObjectDto {

    @JsonProperty("request_type")
    private Integer requestType;
    @JsonProperty("user_roles")
    private List<UserRoleDto> userRoleDtoList;

    private Date updatedOn;
    private Date createdOn;

    private Integer id;
    private SellerDto seller;
    @JsonProperty("entity_version")
    private Integer entityVersion;
    private Integer orderId;
    @JsonProperty("owner_id")
    private Integer ownerId;
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    private String message;
    private Integer version;
    private SellerDto buyer;
    @JsonProperty("request_group_id")
    private String requestGroupId;
    private Double total;
    @JsonProperty("entity_type")
    private String entityType;
    @JsonProperty("request_date")
    private Date requestDate;
    @JsonProperty("chat_room_id")
    private String chatRoomId;
    @JsonProperty("entity_status")
    private String entityStatus;
    @JsonProperty("outstanding_amount")
    private Double outstandingAmount;
    private List<OrderManagementItemsDto> items;
    private Integer status;
    private List<ApprovalsDto> approvals;
    private Boolean active;
    private String statusComment;
    @JsonProperty("created_by")
    private Integer createdBy;
    @JsonProperty("delivery_date")
    private String deliveryDate;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("payment_terms")
    private String paymentTerms;
    @JsonProperty("quality_terms")
    private String qualityTerms;

    private List<FreeFlowPartyDto> freeFlowPartyDtos;

}
