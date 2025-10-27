package com.example.vehicleservice.integration;

import com.example.commondto.dto.request.UserValidationRequest;
import com.example.commondto.dto.response.UserValidationResponse;
import com.example.vehicleservice.kafka.consumer.VehicleConsumer;
import com.example.vehicleservice.kafka.producer.VehicleProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserValidationGateway {

    private final VehicleProducer producer;
    private final VehicleConsumer consumer;

    /**
     * Gửi yêu cầu kiểm tra userId và email tới user-service qua Kafka và trả về phản hồi bất đồng bộ.
     *
     * @param userId ID của người dùng cần kiểm tra
     * @param email  Email của người dùng cần kiểm tra
     * @return CompletableFuture chứa UserValidationResponse
     */
    public CompletableFuture<UserValidationResponse> validateUser(String userId, String email) {
        CompletableFuture<UserValidationResponse> future = new CompletableFuture<>();

        try {
            // Tạo yêu cầu kiểm tra, correlationId tự sinh trong AbstractKafkaMessage
            UserValidationRequest request = UserValidationRequest.builder().userId(userId).email(email).build();

            // Đăng ký callback với correlationId
            consumer.registerCallback(request.getCorrelationId(), response -> {
                if (response != null) {
                    log.info("[Gateway] Received response for correlationId={}: {}", response.getCorrelationId(), response);
                    future.complete(response);
                } else {
                    log.warn("[Gateway] No response received for correlationId={}", request.getCorrelationId());
                    future.complete(createErrorResponse("No response received from user-service"));
                }
            });

            // Gửi yêu cầu tới user-service
            log.info("[Gateway] Sending validation request for userId={}, email={}, correlationId={}", userId, email, request.getCorrelationId());
            producer.sendUserValidationRequest(request);

            // Thêm timeout cho future
            future.orTimeout(30, TimeUnit.SECONDS).exceptionally(throwable -> {
                log.error("[Gateway] Timeout for correlationId={}", request.getCorrelationId(), throwable);
                return createErrorResponse("Timeout waiting for response from user-service");
            });

        } catch (Exception e) {
            log.error("[Gateway] Error during user validation for userId={}, email={}: {}", userId, email, e.getMessage(), e);
            future.complete(createErrorResponse("Internal error during validation"));
        }

        return future;
    }

    /**
     * Tạo phản hồi lỗi với thông điệp được cung cấp.
     *
     * @param message Thông điệp lỗi
     * @return UserValidationResponse với trạng thái không hợp lệ
     */
    private UserValidationResponse createErrorResponse(String message) {
        return UserValidationResponse.builder().valid(false).message(message).build();
    }
}
