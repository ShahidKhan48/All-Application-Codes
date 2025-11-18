package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.ProviderUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface ProviderUserRepository extends JpaRepository<ProviderUser,Integer> {
    List<ProviderUser> findByProviderIdInAndProviderUserIdIn(List<String> provider_id,List<String> provider_user_id);
    List<ProviderUser> findByProviderIdInAndProviderUserIdInAndDeleted(List<String> providerIds,List<String> providerUserIds, int deleted);
    ProviderUser findFirstByProviderIdAndProviderUserIdAndDeleted(String providerId, String providerUserId, int deleted);
    ProviderUser findFirstByIdAndDeleted(Integer id, int deleted);

    List<ProviderUser> findAllByIdInAndDeleted(List<Integer> ids,int deleted);
    ProviderUser findByIdAndDeleted(Integer id, int deleted);

}
