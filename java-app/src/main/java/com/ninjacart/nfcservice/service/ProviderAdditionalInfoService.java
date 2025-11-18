package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.entity.ProviderAdditionalInfo;
import com.ninjacart.nfcservice.repository.ProviderAdditionalInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProviderAdditionalInfoService {

    @Autowired
    ProviderAdditionalInfoRepository providerAdditionalInfoRepository;

    public List<String> getAll()
    {
        List<String> providerIdList = new ArrayList<>();
        List<ProviderAdditionalInfo> providerAdditionalInfoList= providerAdditionalInfoRepository.findAll();
        for(ProviderAdditionalInfo e : providerAdditionalInfoList)
        {
            if(e.getKey().equals("federated") && e.getValue().equals("true")){
                providerIdList.add(e.getProviderId());
            }
        }

        return  providerIdList;
    }
}
