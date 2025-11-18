package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.AccessTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessTokens, String> {

    List<AccessTokens> findAllByUserIdIn(List<String> userIds);
}
