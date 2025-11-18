package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.dtos.ElasticSearchResponseDto;
import com.ninjacart.nfcservice.helpers.RestClientHelper;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.rest.IntegrationRestURL;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ElasticSearchService {

  @Autowired
  private RestTemplateService restTemplateService;

  @Autowired
  private IntegrationRestURL integrationRestURL;

  @Autowired
  private ApplicationConfiguration applicationConfiguration;

  @Autowired
  private RestTemplate restTemplate;

  @LogExecutionTime
  public ElasticSearchResponseDto entitySearch(String entityName, Object searchfilter) {
    String url = integrationRestURL.createSearchUrl(entityName);
    log.debug("getting entity : {} by id : {} : url : {}", entityName, url);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders = new HttpHeaders();
    //TODO remove token hardcoded
    httpHeaders.setBasicAuth(applicationConfiguration.getElasticAuthToken());
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    log.info("headers : {}", httpHeaders);
    ElasticSearchResponseDto response;
    try {
      response = restTemplateService.makeHttpRequest(searchfilter, httpHeaders,
          uriComponentsBuilder, HttpMethod.POST, new ParameterizedTypeReference<ElasticSearchResponseDto>() {
          });
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
}
