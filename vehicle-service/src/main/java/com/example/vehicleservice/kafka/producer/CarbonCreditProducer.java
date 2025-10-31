package com.example.vehicleservice.kafka.producer;

import com.example.commondto.dto.request.UpdateCarbonCreditMessage;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarbonCreditProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUpdateCarbonCredit(UpdateCarbonCreditMessage updateCarbonCreditMessage) {
        kafkaTemplate.send(KafkaTopics.CC_UPDATE_MESSAGE, updateCarbonCreditMessage);
    }
}
