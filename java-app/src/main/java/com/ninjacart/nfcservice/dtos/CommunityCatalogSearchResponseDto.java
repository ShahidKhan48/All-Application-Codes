package com.ninjacart.nfcservice.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.catalog.CatalogGemsResponse;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalog;
import com.ninjacart.nfcservice.dtos.catalog.CommunityCatalogResponse;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class CommunityCatalogSearchResponseDto {

  @JsonProperty("CommunityCatalog")
  private CommunityCatalog communityCatalogResponse;

  private String realmId;
  private String userId;
}
