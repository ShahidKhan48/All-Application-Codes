package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin,String> {
    List<Admin> findByProviderIdInAndUserIdIn(List<String> provider_id, List<String> provider_user_id);

    List<Admin> findByProviderIdIn( List<String> providerId);
    List<Admin> findByProviderIdInAndActiveTrue( List<String> providerId);
    Admin findFirstByProviderIdAndUserIdAndActiveTrue(String providerId, String providerUserId);
}
