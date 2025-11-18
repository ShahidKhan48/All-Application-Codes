package com.ninjacart.nfcservice.configuration;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class OpenApiConfiguration {

  @Value("${nfc.application.open-api-title}")
  private String openApiTitle;
  @Value("${nfc.application.open-api-version}")

  private String openApiVersion;
  @Value("${nfc.application.open-api-description}")

  private String openApiDescription;
  @Value("${nfc.application.open-api-deployed-url}")

  private String openApiDeployedUrl;
}

