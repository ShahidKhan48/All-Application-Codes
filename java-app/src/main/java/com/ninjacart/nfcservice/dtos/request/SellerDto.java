package com.ninjacart.nfcservice.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.ProviderDto;
import com.ninjacart.nfcservice.dtos.productCatalog.ProductCatalog;
import java.util.Date;
import java.util.List;

import com.ninjacart.nfcservice.dtos.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SellerDto {

  @NotEmpty
  private String name;
  @NotNull
  @JsonProperty("app_id")
  private String appId;
  private String phone;
  private String email;
  private Address address;
  @NotEmpty
  @JsonProperty("user_id")
  private String userId;
  @JsonProperty("categories")
  private List<String> categories;
  @JsonProperty("sub_categories")
  private List<SubCategory> subCategories;
  private ProviderDto provider;
  @NotNull
  @JsonProperty("nfc_user_id")
  private Integer nfcUserId;

  private List<ProductCatalog> productCatalog;

  private Date modifiedAt;

  private String facilityId;
}
