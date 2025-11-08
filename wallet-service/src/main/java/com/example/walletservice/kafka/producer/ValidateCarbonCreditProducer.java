package com.example.walletservice.kafka.producer;

import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class ValidateCarbonCreditProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendValidateCarbonCreditResponse(CarbonCreditValidationResponse response) {
        log.info("Sending carbon credit validate response for message:{} -> success={}",
                response.getMessage(), response.isSuccess());
        kafkaTemplate.send(KafkaTopics.CC_VALIDATE_RESPONSE, response);
    }
}
