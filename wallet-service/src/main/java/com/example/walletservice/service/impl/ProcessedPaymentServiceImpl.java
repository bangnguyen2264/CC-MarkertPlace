package com.example.walletservice.service.impl;

import com.example.walletservice.model.entity.ProcessedPayment;
import com.example.walletservice.repository.ProcessedPaymentRepository;
import com.example.walletservice.service.ProcessedPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessedPaymentServiceImpl implements ProcessedPaymentService {

    private final ProcessedPaymentRepository processedPaymentRepository;

    @Override
    public boolean isProcessed(String correlationId) {
        return processedPaymentRepository.existsByCorrelationId(correlationId);
    }

    @Override
    public void markAsProcessed(String correlationId) {
        ProcessedPayment payment = ProcessedPayment.builder()
                .correlationId(correlationId)
                .build();
        processedPaymentRepository.save(payment);
        log.debug("Marked correlationId={} as processed", correlationId);
    }
}
