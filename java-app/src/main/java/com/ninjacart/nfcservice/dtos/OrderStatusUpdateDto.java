package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

public class OrderStatusUpdateDto {
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
}
