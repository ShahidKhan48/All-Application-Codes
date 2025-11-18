package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.*;
import com.ninjacart.nfcservice.dtos.requestGroup.RequestGroupDto;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OrderManagementRestService {
  @Autowired private RestTemplateService restTemplateService;

  @Autowired private IntegrationRestURL integrationRestURL;

  @Autowired private ApplicationConfiguration applicationConfiguration;

  private static final String OUTPUT_TEMPLATE = "outputTemplate";
  @LogExecutionTime
  public List<RequestGroupDto> createRequestGroup(RequestGroupDto requestGroupDto) {
    String url =
        integrationRestURL.getRequestGroupCreationUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating requestGroup by calling url : {}, with body : {}", url, requestGroupDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    ApiResponse<List<RequestGroupDto>> response;
    log.info("start time : {}", new Date().getTime());
    try {
      response =
          restTemplateService.makeHttpRequest(
              Arrays.asList(requestGroupDto),
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<ApiResponse<List<RequestGroupDto>>>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response.getData();
  }
  public void saveMembersOfTheRoom(AddMemberRestCallDto addMemberRestCallDto){
    String url =
            integrationRestURL.saveMembersOfTheRoomUrl(
                    CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("saving to database by calling url : {}, with body : {}", url, addMemberRestCallDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withBasicAuth(
                            applicationConfiguration.getSystemUserName(),
                            applicationConfiguration.getSystemPassword())
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String response;
    log.debug("start time : {}", new Date().getTime());
    try {
      response =
              restTemplateService.makeHttpRequest(
                      addMemberRestCallDto,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.PUT,
                      new ParameterizedTypeReference<String>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

  }
  @LogExecutionTime
  public void updateRequestStatus(UpdateRequestStatusDto updateRequestStatusDto) {
    String url =
        integrationRestURL.getUpdateRequestStatusUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);

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
              updateRequestStatusDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
  }
  @LogExecutionTime
  public void updateConsentStatus(ConsentStatusUpdateDto consentStatusUpdateDto) {
    String url =
        integrationRestURL.getUpdateConsentStatusUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);

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
                  consentStatusUpdateDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
  }
  @LogExecutionTime
  public FetchRequestResponseDto fetchRequest(
      OrderManagementFetchFilter orderManagementFetchFilter, String outputTemplate) {
    String url =
            integrationRestURL.getFetchRequestUrl(
                    CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url).queryParam(OUTPUT_TEMPLATE, outputTemplate);

    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withBasicAuth(
                            applicationConfiguration.getSystemUserName(),
                            applicationConfiguration.getSystemPassword())
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    ApiResponse<FetchRequestResponseDto> response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      orderManagementFetchFilter,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.POST,
                      new ParameterizedTypeReference<ApiResponse<FetchRequestResponseDto>>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return response.getData();
  }
  @LogExecutionTime
  public FetchRequestResponseDto updateOrderStatus(
      FreeFlowFetchUpdateStatusDto FreeFlowFetchUpdateStatusDto, String outputTemplate) {
    String url =
        integrationRestURL.getUpdateStatusUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url)
        .queryParam(OUTPUT_TEMPLATE, outputTemplate);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    ApiResponse<FetchRequestResponseDto> response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              FreeFlowFetchUpdateStatusDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<ApiResponse<FetchRequestResponseDto>>() {
              });
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return response.getData();
  }
}
