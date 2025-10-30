package com.example.verificationservice.kafka;

import com.example.commondto.dto.request.VerifyCreationRequest;
import com.example.commondto.dto.request.VerifyUpdateRequest;
import com.example.commondto.dto.response.VerifyCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateVerifyResponse(VerifyCreationResponse verifyCreationResponse) {
        kafkaTemplate.send(KafkaTopics.VERIFY_CREATION_RESPONSE, verifyCreationResponse);
    }

    public void sendUpdateVerifyRequest(VerifyUpdateRequest verifyUpdateRequest) {
        kafkaTemplate.send(KafkaTopics.VERIFY_UPDATE_MESSAGE, verifyUpdateRequest);
    }
}
