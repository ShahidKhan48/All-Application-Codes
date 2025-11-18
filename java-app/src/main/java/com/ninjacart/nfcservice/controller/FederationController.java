package com.ninjacart.nfcservice.controller;

import com.ninjacart.nfcservice.entity.ProviderAdditionalInfo;
import com.ninjacart.nfcservice.repository.ProviderAdditionalInfoRepository;
import com.ninjacart.nfcservice.service.ProviderAdditionalInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/federated")
public class FederationController {
    @Autowired
    ProviderAdditionalInfoService providerAdditionalInfoService;
    @GetMapping
    public List<String> fetchAllProviderInfo()
    {
        return providerAdditionalInfoService.getAll();
    }
}
