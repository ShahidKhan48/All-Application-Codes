package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.ApiResponse;
import com.ninjacart.nfcservice.dtos.CommandCreationWrapperDto;
import com.ninjacart.nfcservice.dtos.CommandStatusUpdateWrapperDto;
import com.ninjacart.nfcservice.dtos.ConsentsFetchResponseDto;
import com.ninjacart.nfcservice.dtos.OrderManagementFetchFilter;
import com.ninjacart.nfcservice.dtos.OrderManagementRequestResponse;
import com.ninjacart.nfcservice.dtos.RequestCreationResponse;
import com.ninjacart.nfcservice.dtos.SearchRequestDto;
import com.ninjacart.nfcservice.dtos.SearchRequestWorkflowConfigDto;
import com.ninjacart.nfcservice.dtos.SearchResponseDto;
import com.ninjacart.nfcservice.dtos.UserStoreSearchResponseDto;
import com.ninjacart.nfcservice.dtos.request.Message;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.dtos.request.RequestObjectDto;
import com.ninjacart.nfcservice.dtos.request.SellerDto;
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

import java.util.List;

@Service
@Slf4j
public class WorkflowRestService {

  @Autowired private RestTemplateService restTemplateService;

  @Autowired private IntegrationRestURL integrationRestURL;

  @Autowired private ApplicationConfiguration applicationConfiguration;

  @LogExecutionTime
  public RequestCreationResponse createRequest(Message message, Integer userId) {
    Integer providerUserId = CommonConstants.DEFAULT_USERID;
    if(userId!=null){
      providerUserId = userId;
    }
    String url =
        integrationRestURL.getRequestCreationUrl(
            CommonConstants.DEFAULT_REALM, providerUserId);
    log.debug("creating request by calling url : {}, with body : {}", url, message);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add(CommonConstants.SYSTEM_USER_WORKFLOW_HEADER, providerUserId.toString());
    ApiResponse<OrderManagementRequestResponse> response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              message,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<ApiResponse<OrderManagementRequestResponse>>() {});
    } catch (Exception e) {
      log.error("Exception,{}",e);
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    if(response.getData() == null ||  response.getData().getRequest() == null){
      throw CommonUtils.logAndGetException("Something went wrong");
    }
    return response.getData().getRequest();
  }
  @LogExecutionTime
  public SearchResponseDto forwardSearchRequest(SearchRequestDto searchRequestDto) {
    String url =
        integrationRestURL.getNfcForwardSearchUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating request by calling url : {}, with body : {}", url, searchRequestDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    SearchRequestWorkflowConfigDto searchRequestWorkflowConfigDto =
        SearchRequestWorkflowConfigDto.builder().input(searchRequestDto).build();
    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add(CommonConstants.SYSTEM_USER_WORKFLOW_HEADER, String.valueOf(CommonConstants.DEFAULT_USERID));
    ApiResponse<SearchResponseDto> response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              searchRequestWorkflowConfigDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<ApiResponse<SearchResponseDto>>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response.getData();
  }
  @LogExecutionTime
  public void createOrderRequest(CommandCreationWrapperDto commandCreationWrapperDto) {
    String url =
        integrationRestURL.getOrderRequestCreationUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating request by calling url : {}, with body : {}", url, commandCreationWrapperDto);
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
              commandCreationWrapperDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
  }
  @LogExecutionTime
  public void approveOrderRequest(CommandStatusUpdateWrapperDto commandStatusUpdateWrapperDto) {
    String url =
            integrationRestURL.getOrderRequestApprovalUrl(
                    CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating request by calling url : {}, with body : {}", url, commandStatusUpdateWrapperDto);
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
                      commandStatusUpdateWrapperDto,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.POST,
                      new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
  }
  @LogExecutionTime
  public List<ConsentsFetchResponseDto> fetchConsents(OrderManagementFetchFilter orderManagementFetchFilter) {
    String url =
            integrationRestURL.getFetchConsentsUrl(
                    CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating request by calling url : {}, with body : {}", url, orderManagementFetchFilter);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withBasicAuth(
                            applicationConfiguration.getSystemUserName(),
                            applicationConfiguration.getSystemPassword())
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    ApiResponse<List<ConsentsFetchResponseDto>> response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      orderManagementFetchFilter,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.POST,
                      new ParameterizedTypeReference<ApiResponse<List<ConsentsFetchResponseDto>>>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return response.getData();
  }

  @LogExecutionTime
  public UserStoreSearchResponseDto userStoreAdvancedSearch(Object query) {
    String url =
        integrationRestURL.getUserStoreSearchUrl(
            CommonConstants.DEFAULT_REALM, CommonConstants.DEFAULT_USERID);
    log.debug("creating request by calling url : {}, with body : {}", url, query);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withBasicAuth(
                applicationConfiguration.getSystemUserName(),
                applicationConfiguration.getSystemPassword())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    UserStoreSearchResponseDto response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              query,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<UserStoreSearchResponseDto>(){});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return response;
  }
}
