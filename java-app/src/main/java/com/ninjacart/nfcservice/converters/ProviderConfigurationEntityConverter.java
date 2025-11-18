package com.ninjacart.nfcservice.converters;

import com.ninjacart.nfcservice.dtos.ProviderConfigurationDto;
import com.ninjacart.nfcservice.entity.ProviderConfiguration;
import org.springframework.stereotype.Service;

@Service
public class ProviderConfigurationEntityConverter {

    public ProviderConfigurationDto entityToDto(ProviderConfiguration providerConfiguration) {
        if(providerConfiguration == null){
            return ProviderConfigurationDto.builder().build();
        }

        return ProviderConfigurationDto.builder()
                .providerId(providerConfiguration.getProviderId())
                .assignmentTechnique(providerConfiguration.getAssignmentTechnique())
                .categoryFilter(providerConfiguration.getCategoryFilter())
                .disputeApiAuthToken(providerConfiguration.getDisputeApiAuthToken())
                .disputeApiUrl(providerConfiguration.getDisputeApiUrl())
                .fallbackAgentDetails(providerConfiguration.getFallbackAgentDetails())
                .locationFilter(providerConfiguration.getLocationFilter())
                .build();
    }
}
