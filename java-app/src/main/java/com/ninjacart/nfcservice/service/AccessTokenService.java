package com.ninjacart.nfcservice.service;

import com.ninjacart.nfcservice.entity.AccessTokens;
import com.ninjacart.nfcservice.repository.AccessTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    public List<AccessTokens> findAllByUserIds(List<String> matrixUserIds) {
        return accessTokenRepository.findAllByUserIdIn(matrixUserIds);
    }
}
