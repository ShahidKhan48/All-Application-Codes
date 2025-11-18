package com.ninjacart.nfcservice.dtos.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.productCatalog.ProductCatalog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityCatalogResponse {


  private String id;
  private String realmId;
  private String userId;
  private String communityId;
  private String publishedAt;
  private UserInfo publishedBy;
  private List<ProductCatalog> productCatalog;
  private CatalogType catalogType;
  private String eventId;
  private String status;

}


