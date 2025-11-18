package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.ProviderConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderConfigurationRepository extends JpaRepository<ProviderConfiguration, Integer> {

    ProviderConfiguration findFirstByProviderIdAndActiveTrue(String providerId);
}
