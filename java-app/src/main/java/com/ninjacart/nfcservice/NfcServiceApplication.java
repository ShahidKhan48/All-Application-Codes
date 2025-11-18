package com.ninjacart.nfcservice;

import com.ninjacart.nfcservice.configuration.OpenApiConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RequiredArgsConstructor
public class NfcServiceApplication {

  private final OpenApiConfiguration openApiConfiguration;
  public static void main(String[] args) {
    SpringApplication.run(NfcServiceApplication.class, args);
  }

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
    if (CollectionUtils.isEmpty(interceptors)) {
      interceptors = new ArrayList<>();
    }
    restTemplate.setInterceptors(interceptors);
    return restTemplate;
  }
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title(openApiConfiguration.getOpenApiTitle())
        .description(openApiConfiguration.getOpenApiDescription())
        .version(openApiConfiguration.getOpenApiVersion())).servers(Collections.singletonList(
        new Server().url(openApiConfiguration.getOpenApiDeployedUrl())));
  }
}
