package com.ninjacart.nfcservice.service;


import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import com.ninjacart.nfcservice.exception.NFCServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class RestTemplateService {

  private final RestTemplate restTemplate;

  public RestTemplateService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public <T> T makeHttpRequest(Object input, HttpHeaders httpHeaders, UriComponentsBuilder builder,
      HttpMethod verb, ParameterizedTypeReference<T> type) throws RuntimeException {
    HttpEntity<?> response = null;
    String uriString = builder.toUriString();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      httpHeaders.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<?> entity = new HttpEntity<>(input, httpHeaders);
      response = restTemplate.exchange(builder.build().encode().toUri(), verb, entity, type);
      log.debug("Response,{}",response);
      stopWatch.stop();
      long exchangeTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
      log.debug("REST Time {}  {} : {}ms", verb, uriString, exchangeTime);
    } catch (HttpClientErrorException exc) {
      stopWatch.stop();
      long exchangeTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
      log.error("Exception during url:{}, time: {}ms", uriString, exchangeTime, exc);
      handleError(exc);
    } catch (Exception exe) {
      stopWatch.stop();
      long exchangeTime = stopWatch.getTime(TimeUnit.MILLISECONDS);
      log.error("Exception during url:{}, time: {}ms", uriString, exchangeTime, exe);
      throw new NFCServiceException(exe);
    }

    assert response != null;
    return handleSuccess(response);
  }


  private <T> void handleError(HttpClientErrorException exc) throws RuntimeException {
    if (exc.getRootCause() instanceof ConnectException) {
      throw new NFCServiceException(exc);
    }
    if (exc.getRootCause() instanceof SocketTimeoutException) {
      throw new NFCServiceException(exc);
    }
    throw new NFCServiceException(exc);
  }

  private <T> T handleSuccess(HttpEntity<?> response) throws RuntimeException {
//    if (response == null) {
//      throw new ApplicationException(HttpStatus.NO_CONTENT.toString());
//    }n
    return (T) response.getBody();
  }

}
