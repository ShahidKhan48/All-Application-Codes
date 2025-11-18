package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.dtos.CreateRoomDto;
import com.ninjacart.nfcservice.dtos.EditMessageDto;
import com.ninjacart.nfcservice.dtos.JoinUserDto;
import com.ninjacart.nfcservice.dtos.KickUserDto;
import com.ninjacart.nfcservice.dtos.LoginDto;
import com.ninjacart.nfcservice.dtos.MatrixEventResponseDto;
import com.ninjacart.nfcservice.dtos.MessageDto;
import com.ninjacart.nfcservice.dtos.MessageResponse;
import com.ninjacart.nfcservice.dtos.NonceDto;
import com.ninjacart.nfcservice.dtos.OnboardDto;
import com.ninjacart.nfcservice.dtos.OnboardResponseDto;
import com.ninjacart.nfcservice.dtos.RoomCreationResponse;
import com.ninjacart.nfcservice.dtos.UserNameAvailabilityDto;
import com.ninjacart.nfcservice.helpers.RestClientHelper;
import com.ninjacart.nfcservice.helpers.configuration.ApplicationConfiguration;
import com.ninjacart.nfcservice.rest.IntegrationRestURL;
import com.ninjacart.nfcservice.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class MatrixRestService {

  @Autowired private RestTemplateService restTemplateService;

  @Autowired private IntegrationRestURL integrationRestURL;

  @Autowired private ApplicationConfiguration applicationConfiguration;

  @Autowired private RestTemplate restTemplate;
  @LogExecutionTime
  public RoomCreationResponse createRoom(CreateRoomDto createRoomDto) {
    String url = integrationRestURL.roomCreationUrl();
    log.debug("creating room with url : {} and body : {}", url, createRoomDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withAuthorization(applicationConfiguration.getMatrixAdminToken())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    RoomCreationResponse response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              createRoomDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<RoomCreationResponse>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public RoomCreationResponse joinUserToRoom(String roomId, JoinUserDto joinUserDto) {
    String url = integrationRestURL.getJoinUserToRoomUrl(roomId);
    log.debug(
        "forcefully joining user into the room with id : {} with url : {} and body : {}",
        roomId,
        url,
        joinUserDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withAuthorization(applicationConfiguration.getMatrixAdminToken())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    RoomCreationResponse response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              joinUserDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<RoomCreationResponse>() {});
    } catch (Exception e) {
      if(e.getCause() instanceof HttpClientErrorException.Forbidden) {
        return null;
      }
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public OnboardResponseDto onboardUser(OnboardDto onboardDto) {
    String url = integrationRestURL.getMatrixUserOnboardUrl();
    log.debug("onboarding user with url : {} and body : {}", url, onboardDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withAuthorization(applicationConfiguration.getMatrixAdminToken())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    OnboardResponseDto response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              onboardDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<OnboardResponseDto>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public NonceDto getNonce() {
    String url = integrationRestURL.getMatrixNonceUrl();
    log.debug("fetching nonce using url : {} ", url);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    NonceDto response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              null,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.GET,
              new ParameterizedTypeReference<NonceDto>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public MessageResponse sendMessageAsAdmin(
      MessageDto messageDto, String roomId, String uniqueTransactionId) {
    String url = integrationRestURL.sendMessageUrl(roomId, uniqueTransactionId);
    log.debug("sending message as admin with url : {} and body : {}", url, messageDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
        new RestClientHelper()
            .withAuthorization(applicationConfiguration.getMatrixAdminToken())
            .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    MessageResponse response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              messageDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.PUT,
              new ParameterizedTypeReference<MessageResponse>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public MatrixEventResponseDto getEventByIdAndRoomId(String roomId, String eventId, String token) {
    String url = integrationRestURL.getEventByIdAndRoomId(roomId, eventId);
    log.debug("fetching event with : {}", url);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    if(StringUtils.isEmpty(token)) {
      token = applicationConfiguration.getMatrixAdminToken();
    }
    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withAuthorization(token)
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    MatrixEventResponseDto response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      null,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.GET,
                      new ParameterizedTypeReference<MatrixEventResponseDto>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public MessageResponse editMessage(
          EditMessageDto editMessageDto, String roomId, String uniqueTransactionId, String token) {
    String url = integrationRestURL.sendMessageUrl(roomId, uniqueTransactionId);
    log.debug("editing message with url : {} and body : {}", url, editMessageDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    if(StringUtils.isEmpty(token)) {
      token = applicationConfiguration.getMatrixAdminToken();
    }
    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withAuthorization(token)
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    MessageResponse response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      editMessageDto,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.PUT,
                      new ParameterizedTypeReference<MessageResponse>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public MessageResponse sendMessage(
          MessageDto messageDto, String roomId, String uniqueTransactionId, String token) {
    String url = integrationRestURL.sendMessageUrl(roomId, uniqueTransactionId);
    log.debug("sending message as admin with url : {} and body : {}", url, messageDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    if(StringUtils.isEmpty(token)) {
      token = applicationConfiguration.getMatrixAdminToken();
    }
    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withAuthorization(token)
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    MessageResponse response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      messageDto,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.PUT,
                      new ParameterizedTypeReference<MessageResponse>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public boolean checkIfUserNameIsAvailable(String userName) {
    String url = integrationRestURL.getUserNameAvailabilityCheckUrl();

    UriComponentsBuilder uriComponentsBuilder =
        UriComponentsBuilder.fromUriString(url).queryParam("username", userName);
    log.debug("checkIfUserNameIsAvailable uriComponentsBuilder : {} ", uriComponentsBuilder);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    UserNameAvailabilityDto response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              null,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.GET,
              new ParameterizedTypeReference<UserNameAvailabilityDto>() {});
    } catch (Exception e) {
      if (e.getCause() instanceof HttpClientErrorException.BadRequest) {
        return false;
      }
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response.isAvailable();
  }
  @LogExecutionTime
  public OnboardResponseDto login(LoginDto loginDto) {
    String url = integrationRestURL.getMatrixLoginApi();
    if (StringUtils.isEmpty(loginDto.getType())) {
      loginDto.setType("m.login.password");
    }

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    log.debug("login url : {} with dto : {}", url, loginDto);
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    OnboardResponseDto response;
    try {
      response =
          restTemplateService.makeHttpRequest(
              loginDto,
              httpHeaders,
              uriComponentsBuilder,
              HttpMethod.POST,
              new ParameterizedTypeReference<OnboardResponseDto>() {});
    } catch (Exception e) {
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
  @LogExecutionTime
  public boolean checkUserNameAvailability(String userName){
    final String accessToken = applicationConfiguration.getMatrixAdminToken();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("Accept","application/json");
    httpHeaders.set("Authorization", "Bearer " + accessToken);
    HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
    ResponseEntity<Object> response = null;
    try {
    response = restTemplate.exchange(integrationRestURL.checkUserNameAvailability(userName),
            HttpMethod.GET,entity,Object.class);
    }
    catch (HttpClientErrorException e) {
      if (e.getRawStatusCode() == 404) {
        return true;
      }
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }

    return false;

  }

  @LogExecutionTime
  public Object kickUserFromRoom(String roomId, KickUserDto kickUserDto) {
    String url = integrationRestURL.kickUserFromRoom(roomId);
    log.debug("kicking room with url : {} and body : {}", url, kickUserDto);
    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

    HttpHeaders httpHeaders =
            new RestClientHelper()
                    .withAuthorization(applicationConfiguration.getMatrixAdminToken())
                    .build();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    Object response;
    try {
      response =
              restTemplateService.makeHttpRequest(
                      kickUserDto,
                      httpHeaders,
                      uriComponentsBuilder,
                      HttpMethod.POST,
                      new ParameterizedTypeReference<Object>() {});
    } catch (Exception e) {
      if(e.getCause() instanceof HttpClientErrorException.Forbidden) {
        return null;
      }
      throw CommonUtils.logAndGetException("Something went wrong", e);
    }
    return response;
  }
}
