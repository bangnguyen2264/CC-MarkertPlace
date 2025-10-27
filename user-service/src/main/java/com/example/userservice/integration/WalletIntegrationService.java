package com.example.userservice.integration;

import com.example.commondto.dto.request.WalletCreationRequest;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.userservice.kafka.consumer.WalletConsumer;
import com.example.userservice.kafka.producer.WalletProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletIntegrationService {

    private final WalletProducer producer;
    private final WalletConsumer consumer;

    /**
     * Gửi yêu cầu tạo wallet tới wallet-service qua Kafka và trả về phản hồi bất đồng bộ.
     *
     * @param ownerId ID của chủ wallet cần tạo
     * @return CompletableFuture chứa WalletCreationResponse
     */
    public CompletableFuture<WalletCreationResponse> createWallet(String ownerId) {
        CompletableFuture<WalletCreationResponse> future = new CompletableFuture<>();

        try {
            // Tạo yêu cầu tạo wallet, correlationId tự sinh trong AbstractKafkaMessage
            WalletCreationRequest request = new WalletCreationRequest(ownerId, null, "user-service");

            // Đăng ký pending request với correlationId
            CompletableFuture<WalletCreationResponse> consumerFuture = consumer.registerPendingRequest(request.getCorrelationId());

            // Gửi yêu cầu tới wallet-service
            log.info("[Gateway] Sending wallet creation request for ownerId={}, correlationId={}",
                    ownerId, request.getCorrelationId());
            producer.sendCreateWalletRequest(request);

            // Chuyển kết quả từ consumerFuture sang future
            consumerFuture.whenComplete((response, throwable) -> {
                if (throwable != null) {
                    log.error("[Gateway] Error for correlationId={}: {}",
                            request.getCorrelationId(), throwable.getMessage(), throwable);
                    future.complete(createErrorResponse("Error during wallet creation: " + throwable.getMessage()));
                } else if (response != null) {
                    log.info("[Gateway] Received response for correlationId={}: {}",
                            response.getCorrelationId(), response);
                    future.complete(response);
                } else {
                    log.warn("[Gateway] No response received for correlationId={}",
                            request.getCorrelationId());
                    future.complete(createErrorResponse("No response received from wallet-service"));
                }
            });

            // Thêm timeout cho future
            future.orTimeout(30, TimeUnit.SECONDS)
                    .exceptionally(throwable -> {
                        log.error("[Gateway] Timeout for correlationId={}",
                                request.getCorrelationId(), throwable);
                        return createErrorResponse("Timeout waiting for response from wallet-service");
                    });

        } catch (Exception e) {
            log.error("[Gateway] Error during wallet creation for ownerId={}: {}",
                    ownerId, e.getMessage(), e);
            future.complete(createErrorResponse("Internal error during wallet creation"));
        }

        return future;
    }

    /**
     * Tạo phản hồi lỗi với thông điệp được cung cấp.
     *
     * @param message Thông điệp lỗi
     * @return WalletCreationResponse với trạng thái không thành công
     */
    private WalletCreationResponse createErrorResponse(String message) {
        return WalletCreationResponse.builder()
                .success(false)
                .message(message)
                .ownerId(null)
                .build();
    }
}
