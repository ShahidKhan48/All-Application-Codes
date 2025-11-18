package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.exception.NFCServiceException;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TenantRestService {
  @Autowired private RestTemplateService restTemplateService;

  @Autowired private IntegrationRestURL integrationRestURL;

  @Autowired private ApplicationConfiguration applicationConfiguration;

  public void triggerTenantApi(RequestDto requestDto, String baseUrl) {
    //    String url =
    //        integrationRestURL.getTenantRequestCreationUrl(
    //            baseUrl, CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);

    String url = integrationRestURL.getTenantRequestCreationUrl(baseUrl);

    log.debug("triggering tenant api with body : {}", requestDto);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    Object response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              requestDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
  }

  public Object triggerApiToTenant(Object requestBody, String url)  {

    log.debug("triggering tenant api with body : {}", requestBody);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withBasicAuth(
                            applicationConfiguration.getSystemUserName(),
                            applicationConfiguration.getSystemPassword())
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    Object response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      requestBody,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.POST,
                      new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return response;
  }
}
