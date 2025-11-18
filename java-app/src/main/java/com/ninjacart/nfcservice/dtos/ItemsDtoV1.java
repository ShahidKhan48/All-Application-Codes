package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.request.PriceDto;
import com.ninjacart.nfcservice.dtos.request.QuantityDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemsDtoV1 {
    private String id;
    @JsonProperty("item_type")
    private String itemType;
    private String name;
    private QuantityDto quantity;
    private PriceDto price;
    @JsonProperty("image_url")
    private String imageURL;
}
