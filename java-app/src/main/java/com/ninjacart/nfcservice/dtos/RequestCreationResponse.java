package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RequestCreationResponse {

    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    private Integer id;
    @JsonProperty("entity_version")
    private Integer entityVersion;
    @JsonProperty("entity_status")
    private String entityStatus;
    @JsonProperty("request_type")
    private Integer requestType;
    @JsonProperty("user_roles")
    private List<UserRoleDto> userRoleDtoList;
    private SellerDto seller;
    private Integer orderId;
    @JsonProperty("owner_id")
    private Integer ownerId;
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
    @JsonProperty("outstanding_amount")
    private Double outstandingAmount;
    private List<OrderManagementItemsDto> items;
    private Integer status;
    private List<ApprovalsDto> approvals;
    private Object customerRepresentatives;
    private List<FreeFlowPartyDto> freeFlowPartyDtos;

}
