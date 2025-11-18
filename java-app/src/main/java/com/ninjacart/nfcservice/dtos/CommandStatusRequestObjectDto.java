package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.Address;
import com.ninjacart.nfcservice.dtos.request.ItemsDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandStatusRequestObjectDto {


    private SellerDto seller;

    private SellerDto buyer;

    private List<ItemsDtoV1> items;
    @NotEmpty
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    @NotEmpty
    @JsonProperty("command_event_id")
    private String commandEventId;
    @NotEmpty
    @JsonProperty("room_id")
    private String roomId;
    private String status;
    @NotNull
    @JsonProperty("entity_version")
    private Integer entityVersion;
    @NotEmpty
    @JsonProperty("entity_status")
    private String entityStatus;
    @NotNull
    private Integer id;
}
