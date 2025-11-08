package com.example.marketservice.kafka.producer;

import com.example.commondto.dto.request.CarbonCreditValidationRequest;
import com.example.commondto.dto.request.UpdateCarbonCreditMessage;
import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.marketservice.model.entity.MarketListing;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarbonCreditProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendValidateCarbonCreditRequest(CarbonCreditValidationRequest request) {
        log.info("Sending carbon credit validate response for ownerId={} -> success={}",
                request.getSellerId(), request.getCreditId());
        kafkaTemplate.send(KafkaTopics.CC_VALIDATE_REQUEST, request);
    }

    public void sendUpdateCarbonCreditRequest(UpdateCarbonCreditMessage message) {
        log.info("Sending carbon credit validate response for ownerId={} -> newTotalCredit={}, newTraderCredit={}",
                message.getOwnerId(), message.getNewTotalCredit(), message.getNewTradedCredit());
        kafkaTemplate.send(KafkaTopics.CC_UPDATE_MESSAGE, message);    }

    public void sendPurchaseEvent(MarketListing listing, String buyerId) {
    }
}
