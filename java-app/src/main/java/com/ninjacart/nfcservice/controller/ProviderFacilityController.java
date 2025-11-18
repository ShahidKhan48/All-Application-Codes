package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.dtos.FetchFacilityResponseDto;
import com.ninjacart.nfcservice.entity.ProviderFacility;
import com.ninjacart.nfcservice.service.ProviderFacilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facility")
public class ProviderFacilityController {
    @Autowired
    private ProviderFacilityService facilityService;

    @GetMapping
    public List<FetchFacilityResponseDto> getFacility(@RequestParam String providerId,@RequestParam String city , @RequestParam(required = false) String falicityName){
        return facilityService.getAllFacility(providerId,city,falicityName);
    }
}
