package com.ninjacart.nfcservice.repository;

import com.ninjacart.nfcservice.entity.ProviderAdditionalInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
;@Repository
public interface ProviderAdditionalInfoRepository extends JpaRepository<ProviderAdditionalInfo,Integer> {
}
