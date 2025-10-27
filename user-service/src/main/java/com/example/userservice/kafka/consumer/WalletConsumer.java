package com.example.userservice.kafka.consumer;

import com.example.commondto.dto.response.WalletCreationResponse;
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
public class WalletConsumer {

    // Dùng map để lưu future theo correlationId
    private final ConcurrentHashMap<String, CompletableFuture<WalletCreationResponse>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Đăng ký pending request với correlationId.
     *
     * @param correlationId ID duy nhất của request
     * @return CompletableFuture để nhận response
     */
    public CompletableFuture<WalletCreationResponse> registerPendingRequest(String correlationId) {
        CompletableFuture<WalletCreationResponse> future = new CompletableFuture<>();
        // Thêm timeout để tránh treo
        future.orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(throwable -> {
                    log.error("⚠️ Timeout waiting for response with correlationId={}", correlationId, throwable);
                    pendingRequests.remove(correlationId);
                    return null;
                });
        pendingRequests.put(correlationId, future);
        log.info("Registered pending request for correlationId={}", correlationId);
        return future;
    }

    @KafkaListener(
            topics = KafkaTopics.WALLET_CREATION_RESPONSE,
            groupId = "${spring.application.name}-group",
            containerFactory = "walletCreationKafkaListenerFactory"
    )
    public void consumeWalletCreationResponse(WalletCreationResponse response) {
        log.info("📥 Received wallet creation response with correlationId={} for ownerId={} -> success={}, message={}",
                response.getCorrelationId(), response.getOwnerId(), response.isSuccess(), response.getMessage());

        CompletableFuture<WalletCreationResponse> future = pendingRequests.remove(response.getCorrelationId());
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("⚠️ No pending request found for correlationId={}", response.getCorrelationId());
        }
    }
}