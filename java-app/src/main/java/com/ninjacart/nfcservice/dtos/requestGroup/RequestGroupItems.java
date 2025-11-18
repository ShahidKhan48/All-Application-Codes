package com.ninjacart.nfcservice.dtos.requestGroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestGroupItems {

    private String itemName;
    private String itemId;
    private double quantity;
    private double price;
    @JsonProperty("image_url")
    private String imageUrl;
}
