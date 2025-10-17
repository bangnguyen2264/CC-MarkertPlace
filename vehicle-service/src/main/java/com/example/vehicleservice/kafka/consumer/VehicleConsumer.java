package com.example.vehicleservice.kafka.consumer;


import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class VehicleConsumer {

    @Getter
    private volatile UserValidationResponse lastResponse;
    private Consumer<UserValidationResponse> callback;

    @KafkaListener(
            topics = KafkaTopics.USER_VALIDATION_RESPONSE,
            groupId = "${spring.application.name}-group"
    )
    public void consumeUserValidationResponse(UserValidationResponse response) {
        log.info("Received validation response from user-service: {}", response);
        this.lastResponse = response;

        if (callback != null) {
            try {
                callback.accept(response);
            } catch (Exception e) {
                log.error("Error executing callback", e);
            }
        }
    }

    public void registerCallback(Consumer<UserValidationResponse> callback) {
        this.callback = callback;
        log.info("Callback registered");
    }
}