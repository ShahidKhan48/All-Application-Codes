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
public class CommandCreationObjectDto {

    @NotNull
    @Valid
    private SellerDto seller;
    @NotNull
    @Valid
    private SellerDto buyer;
    @NotNull
    @Valid
    private List<ItemsDto> items;
    @NotEmpty
    @JsonProperty("external_reference_id")
    private String externalReferenceId;
    @JsonProperty("delivery_date")
    private String deliveryDate;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @NotEmpty
    @JsonProperty("chat_room_id")
    private String chatRoomId;
    @NotNull
    @JsonProperty("entity_version")
    private Integer entityVersion;
    @NotEmpty
    @JsonProperty("entity_status")
    private String entityStatus;
    @NotNull
    private Integer id;

}
