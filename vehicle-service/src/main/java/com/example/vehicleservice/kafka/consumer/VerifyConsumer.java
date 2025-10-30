package com.example.vehicleservice.kafka.consumer;

import com.example.commondto.dto.response.VerifyCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifyConsumer {

    private final ConcurrentHashMap<String, CompletableFuture<VerifyCreationResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Đăng ký pending request với correlationId.
     *
     * @param correlationId ID duy nhất của request
     * @return CompletableFuture để nhận response
     */
    public CompletableFuture<VerifyCreationResponse> registerPendingRequest(String correlationId) {
        CompletableFuture<VerifyCreationResponse> future = new CompletableFuture<>();

        future.orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("Timeout waiting for verify response. correlationId={}", correlationId, throwable);
                    pendingRequests.remove(correlationId);
                    return null;
                });

        pendingRequests.put(correlationId, future);
        log.info("Registered pending verify request. correlationId={}", correlationId);

        return future;
    }

    @KafkaListener(
            topics = KafkaTopics.VERIFY_CREATION_RESPONSE,
            groupId = "${spring.application.name}-group",
            containerFactory = "verifyCreationResponseKafkaListenerFactory"
    )
    public void consumeVerifyCreationResponse(VerifyCreationResponse response) {
        String correlationId = response.getCorrelationId();
        log.info("Received verify creation response. correlationId={}",
                correlationId);

        CompletableFuture<VerifyCreationResponse> future = pendingRequests.remove(correlationId);

        if (future != null) {
            future.complete(response);
        } else {
            log.warn("No pending verify request found. correlationId={}", correlationId);
        }
    }



}
