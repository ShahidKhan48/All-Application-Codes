package com.ninjacart.nfcservice.dtos.productCatalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductCatalog {

  private String id;
  private String realmId;
  private String userId;
  private Descriptor descriptor;
  private Price price;
  private boolean active;
  private Quantity quantity;
  private String catalogType;
  private UserInfo userInfo;
  private String providerToolId;
  private Date modifiedAt;


}