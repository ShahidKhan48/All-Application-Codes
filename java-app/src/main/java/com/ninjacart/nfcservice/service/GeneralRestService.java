package com.ninjacart.nfcservice.service;
import com.ninjacart.nfcservice.annotations.LogExecutionTime;
import com.ninjacart.nfcservice.constants.CommonConstants;
import com.ninjacart.nfcservice.dtos.OAuthResponse;
import com.ninjacart.nfcservice.dtos.ProviderDisputeDto;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
@Slf4j
@Service
public class GeneralRestService {
    @Autowired
    private RestTemplateService restTemplateService;

    @Autowired
    private IntegrationRestURL integrationRestURL;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";

    @LogExecutionTime
    public Object providerDisputeUrl(String url, String authToken, ProviderDisputeDto providerDisputeDto) {
        log.debug("calling dispute url : {} by utl : {} : authToken : {}", url, authToken);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
        HttpHeaders httpHeaders =
                new RestClientHelper()
                        .withAuthorization(
                                authToken)
                        .build();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Object response;
        try {
            response =
                    restTemplateService.makeHttpRequest(
                            providerDisputeDto,
                            httpHeaders,
                            uriComponentsBuilder,
                            HttpMethod.GET,
                            new ParameterizedTypeReference<Object>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }
    @LogExecutionTime
    public OAuthResponse getOAuthToken() {
        String url = integrationRestURL.getOAuthUrl();
        String authToken = applicationConfiguration.getOAuthToken();
        log.debug("getting token by url : {} : authToken : {}", url, authToken);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url).queryParam(GRANT_TYPE, CLIENT_CREDENTIALS);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CommonConstants.AUTHORIZATION, authToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        OAuthResponse response;
        try {
            response =
                    restTemplateService.makeHttpRequest(
                            null,
                            httpHeaders,
                            uriComponentsBuilder,
                            HttpMethod.POST,
                            new ParameterizedTypeReference<OAuthResponse>() {});
        } catch (Exception e) {
            throw CommonUtils.logAndGetException("Something went wrong", e);
        }
        return response;
    }
}