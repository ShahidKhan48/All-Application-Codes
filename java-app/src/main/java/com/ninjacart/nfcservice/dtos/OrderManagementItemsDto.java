package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderManagementItemsDto {
    private Integer id;
    private String itemName;
    private Double quantity;
    private String imageURL;
    private Integer itemTypeId;
    private Double unitBuyPrice;
    private String itemUomName;
    private String packing;
}
