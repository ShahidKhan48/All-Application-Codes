package com.ninjacart.nfcservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemsDto {

  private String id;
  @JsonProperty("item_type")
  private String itemType;
  @NotEmpty
  private String name;
  @NotNull
  @Valid
  private QuantityDto quantity;
  @NotNull
  @Valid
  private PriceDto price;
  @JsonProperty("image_url")
  private String imageURL;
  @JsonProperty("uom_name")
  private String uomName;
  private String packing;
  private String category;
}
