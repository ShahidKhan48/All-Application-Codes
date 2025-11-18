package com.ninjacart.nfcservice.rest;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BaseURLConfiguration {

  @Value("${baseUrls.matrix:https://qa-matrix.ninjacart.in}")
  private String matrixBaseUrl;
  @Value("${baseUrls.orderManagement:https://qa.ninjacart.in/ordermanagement}")
  private String orderManagementBaseUrl;
  @Value("${baseUrls.workflowUrl:https://qa.ninjacart.in/workflow-engine}")
  private String workflowBaseUrl;
  @Value("${baseUrls.gemsBaseUrl:https://agnet-sandbox.ninjacart.in/entity-service}")
  private String gemsBaseUrl;

  @Value("${baseUrls.elasticSearchBaseUrl:https://search-es-hz.ninjacart.in}")
  private String elasticSearchUrl;

  @Value("${baseUrls.nfc:https://qa-nfc.ninjacart.in/ninja-agnet-web/home}")
  private String nfcBaseUrl;
  @Value("${baseUrls.oAuthBaseUrl:https://agnet-sandbox.ninjacart.in}")
  private String oAuthBaseUrl;
}
