package com.ninjacart.nfcservice.dtos.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ninjacart.nfcservice.dtos.productCatalog.ProductCatalog;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityCatalog {
    private String id;
    @JsonProperty("realm_id")
    private String realmId;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("community_id")
    private String communityId;
    @JsonProperty("published_at")
    private String publishedAt;
    @JsonProperty("published_by")
    private UserInfo publishedBy;
    @JsonProperty("product_catalog")
    private List<ProductCatalog> productCatalog;
    @JsonProperty("catalog_type")
    private CatalogType catalogType;
    private String eventId;
    private String status;
    private int expiryDays;
    private Date expiryAt;
    private boolean isExpired;
}
