package com.example.walletservice.repository;

import com.example.walletservice.model.entity.ProcessedPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedPaymentRepository extends JpaRepository<ProcessedPayment, String> {
    boolean existsByCorrelationId(String correlationId);
}
