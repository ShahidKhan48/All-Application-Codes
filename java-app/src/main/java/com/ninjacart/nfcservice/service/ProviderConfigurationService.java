package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.converters.ProviderConfigurationEntityConverter;
import com.ninjacart.nfcservice.dtos.ProviderConfigurationDto;
import com.ninjacart.nfcservice.entity.ProviderConfiguration;
import com.ninjacart.nfcservice.repository.ProviderConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class ProviderConfigurationService {

  @Autowired private ProviderConfigurationRepository providerConfigurationRepository;

  @Autowired private ProviderConfigurationEntityConverter providerConfigurationEntityConverter;

  public ProviderConfigurationDto fetchProviderConfig(String providerId) {
    ProviderConfiguration providerConfiguration =
        providerConfigurationRepository.findFirstByProviderIdAndActiveTrue(providerId);
    return providerConfigurationEntityConverter.entityToDto(providerConfiguration);
  }

  public ProviderConfiguration createOrUpdate(
      Integer userId, ProviderConfigurationDto providerConfigurationDto) {
    ProviderConfiguration providerConfiguration =
        providerConfigurationRepository.findFirstByProviderIdAndActiveTrue(
            providerConfigurationDto.getProviderId());

    if (providerConfiguration == null) {
      providerConfiguration = createEntity(userId, providerConfigurationDto);
    } else {
      providerConfiguration = setDtoDetails(userId, providerConfiguration, providerConfigurationDto);
    }
    return providerConfigurationRepository.save(providerConfiguration);
  }

  private ProviderConfiguration setDtoDetails(
      Integer userId,
      ProviderConfiguration providerConfiguration,
      ProviderConfigurationDto providerConfigurationDto) {
    providerConfiguration.setAssignmentTechnique(providerConfigurationDto.getAssignmentTechnique());
    providerConfiguration.setCategoryFilter(providerConfigurationDto.getCategoryFilter());
    providerConfiguration.setLocationFilter(providerConfigurationDto.getLocationFilter());
    providerConfiguration.setFallbackAgentDetails(
        providerConfigurationDto.getFallbackAgentDetails());
    providerConfiguration.setDisputeApiUrl(providerConfigurationDto.getDisputeApiUrl());
    providerConfiguration.setDisputeApiAuthToken(providerConfigurationDto.getDisputeApiAuthToken());
    providerConfiguration.setUpdatedBy(userId);
    providerConfiguration.setUpdatedAt(new Date());
    return providerConfiguration;
  }

  private ProviderConfiguration createEntity(
      Integer userId, ProviderConfigurationDto providerConfigurationDto) {
    return ProviderConfiguration.builder()
        .disputeApiAuthToken(providerConfigurationDto.getDisputeApiAuthToken())
        .active(true)
        .createdAt(new Date())
        .createdBy(userId)
        .updatedBy(userId)
        .updatedAt(new Date())
        .providerId(providerConfigurationDto.getProviderId())
        .assignmentTechnique(providerConfigurationDto.getAssignmentTechnique())
        .categoryFilter(providerConfigurationDto.getCategoryFilter())
        .locationFilter(providerConfigurationDto.getLocationFilter())
        .disputeApiUrl(providerConfigurationDto.getDisputeApiUrl())
        .fallbackAgentDetails(providerConfigurationDto.getFallbackAgentDetails())
        .disputeApiAuthToken(providerConfigurationDto.getDisputeApiAuthToken())
        .build();
  }
}
