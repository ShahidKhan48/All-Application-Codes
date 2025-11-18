package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.dtos.FetchFacilityResponseDto;
import com.ninjacart.nfcservice.entity.ProviderFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProviderFacilityRepository extends JpaRepository<ProviderFacility,Integer> {

    List<ProviderFacility> findByProviderIdAndCityName(String providerId, String cityName);
    List<ProviderFacility>
            findByProviderIdAndCityNameAndFacilityNameContaining(String providerId,
                                                                              String cityName,
                                                                              String facilityName);
}
