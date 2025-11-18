package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.ProviderConfigurationDto;
import com.ninjacart.nfcservice.service.ProviderConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/{realm_id}/{user_id}/provider_configuration")
public class ProviderConfigurationController {

  private static final String UPDATE_API_RESPONSE = "Success";

  @Autowired private ProviderConfigurationService providerConfigurationService;

  @PostMapping
  public String createOrUpdateProviderConfiguration(
      @PathVariable(name = "user_id") Integer userId,
      @RequestBody @Valid ProviderConfigurationDto providerConfigurationDto)
      throws Exception {
    providerConfigurationService.createOrUpdate(userId, providerConfigurationDto);
    return UPDATE_API_RESPONSE;
  }

  @GetMapping("/{provider_id}")
  public ProviderConfigurationDto fetchProviderConfiguration(
      @PathVariable(name = "provider_id") String providerId) throws Exception {
    return providerConfigurationService.fetchProviderConfig(providerId);
  }
}
