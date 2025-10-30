package com.example.vehicleservice.integration;

import com.example.commondto.dto.request.VerifyCreationRequest;
import com.example.commondto.dto.response.VerifyCreationResponse;
import com.example.vehicleservice.kafka.consumer.VerifyConsumer;
import com.example.vehicleservice.kafka.producer.VerifyProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyCreationIntegration {

    private final VerifyConsumer consumer;
    private final VerifyProducer producer;

    public CompletableFuture<VerifyCreationResponse> createVerify(VerifyCreationRequest request) {
        log.info("[Gateway] Sending verify creation request for referenceId={}, userId={}, correlationId={}",
                request.getReferenceId(), request.getUserId(), request.getCorrelationId());

        CompletableFuture<VerifyCreationResponse> future = consumer.registerPendingRequest(request.getCorrelationId());

        try {
            producer.sendCreateVerifyRequest(request);
        } catch (Exception e) {
            log.error("[Gateway] Failed to send verify creation request. correlationId={}",
                    request.getCorrelationId(), e);
            future.complete(createErrorResponse("Failed to send request to verification-service"));
        }

        future.orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("[Gateway] Timeout waiting for verify response. correlationId={}",
                            request.getCorrelationId(), throwable);
                    return createErrorResponse("Timeout waiting for response from verification-service");
                });

        return future;
    }

    private VerifyCreationResponse createErrorResponse(String message) {
        return VerifyCreationResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
