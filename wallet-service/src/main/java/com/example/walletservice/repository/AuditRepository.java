package com.example.walletservice.repository;

import com.example.walletservice.model.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditRepository extends JpaRepository<Audit, String>, JpaSpecificationExecutor<Audit> {
}
