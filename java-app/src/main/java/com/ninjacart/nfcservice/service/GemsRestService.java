package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.*;
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
public class GemsRestService {

    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired private IntegrationRestURL integrationRestURL;

    @Autowired private ApplicationConfiguration applicationConfiguration;

    @Autowired private RestTemplate restTemplate;

    @Autowired private GeneralRestService generalRestService;

    @LogExecutionTime
    public GemsEntityResponseDto findGemsEntityById(String id, String realmId, String userId, String entityName) {
        String url = integrationRestURL.findByIdGemsUrl(realmId, userId, entityName, id);
        log.debug("getting entity : {} by id : {} : url : {}", entityName, id, url);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        GemsEntityResponseDto response;
        try {
            response =
                    restTemplateService.makeHttpRequest(
                            null,
                            httpHeaders,
                            uriComponentsBuilder,
                            HttpMethod.GET,
                            new ParameterizedTypeReference<GemsEntityResponseDto>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }

    @LogExecutionTime
    public GemsEntityResponseDto updateGemsEntityById(String id, String realmId, String userId, String entityName, Object body) {
        String url = integrationRestURL.findByIdGemsUrl(realmId, userId, entityName, id);
        log.debug("getting entity : {} by id : {} : url : {} : entity : {}", entityName, id, url,body);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        GemsEntityResponseDto response;
        try {
            response =
                    restTemplateService.makeHttpRequest(
                            body,
                            httpHeaders,
                            uriComponentsBuilder,
                            HttpMethod.PUT,
                            new ParameterizedTypeReference<GemsEntityResponseDto>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }
    @LogExecutionTime
    public GemsEntityResponseDto createGemsEntity(String realmId, String userId, String entityName, GemsEntityResponseDto communityEntityDto) {
        String url = integrationRestURL.createCommunityUrl(realmId, userId, entityName);
        log.debug("getting entity : {} by id : {} : url : {}", entityName, url);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        GemsEntityResponseDto response;
        try {
            response =
                    restTemplateService.makeHttpRequest(
                            communityEntityDto,
                            httpHeaders,
                            uriComponentsBuilder,
                            HttpMethod.POST,
                            new ParameterizedTypeReference<GemsEntityResponseDto>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }

    @LogExecutionTime
    public Object entityAdvancedSearch(String realmId, String userId, String entityName, Object searchfilter) {
        String url = integrationRestURL.createAdvancedSearchUrl(realmId, userId, entityName);
        log.debug("getting entity : {} by id : {} : url : {}", entityName, url);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Object response;
        try {
            response =
                restTemplateService.makeHttpRequest(
                    searchfilter,
                    httpHeaders,
                    uriComponentsBuilder,
                    HttpMethod.POST,
                    new ParameterizedTypeReference<Object>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        if(applicationConfiguration.isOAuthEnabled()) {
            OAuthResponse oAuthResponse = generalRestService.getOAuthToken();
            httpHeaders.set(CommonConstants.AUTHORIZATION, String.format("%s %s", oAuthResponse.getTokenType(), oAuthResponse.getAccessToken()));
            log.debug("http headers : {}", httpHeaders);
        }
        else {
            httpHeaders.setBasicAuth(applicationConfiguration.getSystemUserName(), applicationConfiguration.getSystemPassword());
        }
        return httpHeaders;
    }
}
