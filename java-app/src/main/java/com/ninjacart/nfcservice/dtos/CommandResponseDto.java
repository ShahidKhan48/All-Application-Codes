package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandResponseDto {

  private InitiatedByDto initiatedBy;
  private Date updatedOn;
  private Date createdOn;
  private Integer id;
  private SellerDto seller;
  private SellerDto buyer;



  @JsonProperty("entity_version")
  private Integer entityVersion;
  private Integer orderId;
  @JsonProperty("owner_id")
  private Integer ownerId;
  @JsonProperty("external_reference_id")
  private String externalReferenceId;
  private String message;
  @JsonProperty("chat_room_id")
  private String chatRoomId;
  @JsonProperty("entity_status")
  private String entityStatus;
  @JsonProperty("status_comment")
  private String statusComment;

}
