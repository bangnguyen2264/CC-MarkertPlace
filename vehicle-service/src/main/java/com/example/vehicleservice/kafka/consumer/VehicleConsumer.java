package com.example.vehicleservice.kafka.consumer;


import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
@Service
@Slf4j
public class VehicleConsumer {

    // Map lưu callback theo correlationId
    private final ConcurrentHashMap<String, Consumer<UserValidationResponse>> callbacks = new ConcurrentHashMap<>();

    @KafkaListener(
            topics = KafkaTopics.USER_VALIDATION_RESPONSE,
            groupId = "${spring.application.name}-group",
            containerFactory = "userValidationResponseKafkaListenerFactory"
    )
    public void consumeUserValidationResponse(UserValidationResponse response) {
        log.info("📥 Received validation response from user-service with correlationId={}: {}",
                response.getCorrelationId(), response);

        // Lấy callback tương ứng với correlationId
        Consumer<UserValidationResponse> callback = callbacks.remove(response.getCorrelationId());
        if (callback != null) {
            try {
                callback.accept(response);
            } catch (Exception e) {
                log.error("Error executing callback for correlationId={}", response.getCorrelationId(), e);
            }
        } else {
            log.warn("⚠️ No callback found for correlationId={}", response.getCorrelationId());
        }
    }

    public void registerCallback(String correlationId, Consumer<UserValidationResponse> callback) {
        callbacks.put(correlationId, callback);
        log.info("Callback registered for correlationId={}", correlationId);
    }
}