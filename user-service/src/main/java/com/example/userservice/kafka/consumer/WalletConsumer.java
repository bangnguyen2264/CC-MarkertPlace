package com.example.userservice.kafka.consumer;

import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    // Dùng map để tạm lưu phản hồi tương ứng với từng ownerId
    private final ConcurrentHashMap<String, CompletableFuture<WalletCreationResponse>> pendingRequests = new ConcurrentHashMap<>();

    public void registerPendingRequest(String ownerId, CompletableFuture<WalletCreationResponse> future) {
        pendingRequests.put(ownerId, future);
    }

    @KafkaListener(
            topics = KafkaTopics.WALLET_CREATION_RESPONSE,
            groupId = "${spring.application.name}-group"
    )
    public void consumeWalletCreationResponse(WalletCreationResponse response) {
        log.info("📥 Received wallet creation response for ownerId={} -> success={}, message={}",
                response.getOwnerId(), response.isSuccess(), response.getMessage());

        CompletableFuture<WalletCreationResponse> future = pendingRequests.remove(response.getOwnerId());
        if (future != null) {
            future.complete(response);
        } else {
            log.warn("⚠️ No pending request found for ownerId={}", response.getOwnerId());
        }
    }
}

