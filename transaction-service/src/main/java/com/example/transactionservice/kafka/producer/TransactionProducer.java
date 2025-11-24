package com.example.transactionservice.kafka.producer;

import com.example.commondto.dto.request.PaymentEvent;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(String listingId) {
        log.info("Sending payment event to listing " + listingId);
        kafkaTemplate.send(KafkaTopics.MARKET_PAYMENT_EVENT, listingId);
    }
}
