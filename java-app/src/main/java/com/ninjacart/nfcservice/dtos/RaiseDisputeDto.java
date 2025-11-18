package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;
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

public class RaiseDisputeDto {


  @NotEmpty
  @JsonProperty("external_reference_id")
  private String externalReferenceId;
  @JsonProperty("initiated_by")
  private InitiatedByDto initiatedBy;
  @JsonProperty("entity_status")
  private String entityStatus;
  @NotEmpty
  @JsonProperty("entity_version")
  private Integer entityVersion;
  private String providerId;
  @JsonProperty("chat_room_id")
  private String chatRoomId;
  @JsonProperty("dispute_message")
  private String disputeMessage;
}


