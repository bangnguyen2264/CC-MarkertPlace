package com.example.walletservice.repository;

import com.example.walletservice.model.entity.CarbonCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CarbonCreditRepository extends JpaRepository<CarbonCredit, String>, JpaSpecificationExecutor<CarbonCredit> {
    Optional<CarbonCredit> findByOwnerId(String ownerId);
}
