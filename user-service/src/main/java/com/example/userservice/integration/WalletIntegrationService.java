package com.example.userservice.integration;

import com.example.commondto.dto.request.WalletCreationRequest;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.userservice.kafka.consumer.WalletConsumer;
import com.example.userservice.kafka.producer.WalletProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletIntegrationService {

    private final WalletProducer walletProducer;
    private final WalletConsumer walletConsumer;

    // Thời gian tối đa chờ phản hồi từ Kafka (ms)
    private static final long TIMEOUT_MS = 10000;

    public WalletCreationResponse createWalletForUser(String ownerId) {
        log.info("🚀 Sending wallet creation request for ownerId={}", ownerId);

        CompletableFuture<WalletCreationResponse> future = new CompletableFuture<>();
        walletConsumer.registerPendingRequest(ownerId, future);

        walletProducer.sendCreateWalletRequest(new WalletCreationRequest(
                ownerId,null,"user-service"
        ));

        try {
            // Chờ phản hồi trong giới hạn thời gian
            WalletCreationResponse response = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            log.info("✅ Wallet creation result: {}", response);
            return response;
        } catch (Exception e) {
            log.error("❌ Timeout or error while waiting for wallet creation response", e);
            return WalletCreationResponse.builder()
                    .ownerId(ownerId)
                    .success(false)
                    .message("Timeout or error waiting for response")
                    .build();
        }
    }
}
