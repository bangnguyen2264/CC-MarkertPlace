package com.example.userservice.kafka.producer;

import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendValidationResponse(UserValidationResponse response) {
        log.info("Sending validation response: {}", response);
        kafkaTemplate.send(KafkaTopics.USER_VALIDATION_RESPONSE, response);
    }
}
