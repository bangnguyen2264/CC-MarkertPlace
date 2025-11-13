package com.example.marketservice.kafka.consumer;

import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.marketservice.integration.CarbonCreditValidationIntegration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarbonCreditValidateConsumer {

    private final CarbonCreditValidationIntegration validationIntegration;

    @KafkaListener(
            topics = KafkaTopics.CC_VALIDATE_RESPONSE,
            groupId = "${spring.application.name}-group",
            containerFactory = "carbonCreditValidateKafkaListenerFactory"
    )
    public void consumeCarbonCreditValidateResponse(CarbonCreditValidationResponse response) {
        log.info("📥 Received CarbonCredit validation response: {}", response);
        validationIntegration.completeResponse(response);
    }


}
