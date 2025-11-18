package com.ninjacart.nfcservice.helpers.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ApplicationConfiguration {

  @Value("${matrix.adminToken:syt_YWRtaW4_iRamkRRbUurnJdLnQHwt_2HihsF}")
  private String matrixAdminToken;

  @Value("${system.user.name:mayank}")
  private String systemUserName;

  @Value("${system.user.password:cr@zym@rvel}")
  private String systemPassword;

  @Value("${matrix.nfcAdminUserId:22}")
  private Integer nfcAdminUserId;

  @Value("${nfc.providerId:25c3ed73-cd4b-4dac-8a7c-473a6e359f38}")
  private String nfcProviderId;

  @Value("${nfc.adminUserId:1711043}")
  private Integer adminUserId;

  @Value("${oauth.authToken:Basic YWduZXQtdGVzdC1jbGktMTpOVzZXMlFZeVJvNGJKSnp0OUVScQ==}")
  private String oAuthToken;

  @Value("${oauth.enabled:true}")
  private boolean oAuthEnabled;

  @Value("${elastic.basic.authToken:ZWxhc3RpYzo4dy1KcjlzM0lKVWpWNWpDTG1reA==}")
  private String elasticAuthToken;
}
