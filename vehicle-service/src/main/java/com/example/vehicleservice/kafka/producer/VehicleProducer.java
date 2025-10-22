package com.example.vehicleservice.kafka.producer;


import com.example.commondto.dto.request.UserValidationRequest;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateWalletResponse(boolean success) {
        kafkaTemplate.send(KafkaTopics.WALLET_CREATION_RESPONSE, success);
    }
}
