package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.ApiResponse;
import com.ninjacart.nfcservice.dtos.request.ClientRequestDto;
import com.ninjacart.nfcservice.dtos.request.RequestDto;
import com.ninjacart.nfcservice.service.RequestNegotiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Validated
@RestController
@RequestMapping("/{realm_id}/{user_id}/request-negotiation")
public class RequestNegotiationController {

  @Autowired private RequestNegotiationService requestNegotiationService;

  @PostMapping
  public RequestDto requestNegotiation(@RequestBody @Valid RequestDto requestDto)
      throws NoSuchAlgorithmException, InvalidKeyException {
    return requestNegotiationService.requestNegotiation(requestDto);
  }

  @PostMapping("/bulk")
  public ApiResponse<String> createRequest(@RequestBody @Valid ClientRequestDto clientRequestDto) {
    return new ApiResponse<String>(requestNegotiationService.forwardRequest(clientRequestDto));
  }
}
