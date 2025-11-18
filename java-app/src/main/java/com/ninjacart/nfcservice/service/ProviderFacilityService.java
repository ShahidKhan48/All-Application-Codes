package com.ninjacart.nfcservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ninjacart.nfcservice.dtos.FetchFacilityResponseDto;
import com.ninjacart.nfcservice.entity.ProviderFacility;
import com.ninjacart.nfcservice.repository.ProviderFacilityRepository;
import com.ninjacart.nfcservice.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderFacilityService {
    @Autowired
    ProviderFacilityRepository providerFacilityRepository;
    public List<FetchFacilityResponseDto> getAllFacility(String providerId,String city,String facilityName) {
        List<ProviderFacility> providerFacilityList = new ArrayList<>();

        if(StringUtils.isEmpty(providerId) || StringUtils.isEmpty(city)){
            throw  CommonUtils.logAndGetException("providerId and city name can not be empty");
        }else if(StringUtils.isNotEmpty(facilityName)){
            providerFacilityList= providerFacilityRepository.findByProviderIdAndCityNameAndFacilityNameContaining(
                    providerId,city,facilityName
            );
        }
        else  {
            providerFacilityList= providerFacilityRepository.
            findByProviderIdAndCityName(providerId,city);
        }

        List<FetchFacilityResponseDto> fetchFacilityResponseDtoList = providerFacilityList.stream().
                map(o -> new FetchFacilityResponseDto(o.getProviderId(),
                        o.getFacilityId(), o.getFacilityName()))
                .collect(Collectors.toList());

        return fetchFacilityResponseDtoList;
    }
}
