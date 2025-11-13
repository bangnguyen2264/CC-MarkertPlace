package com.example.transactionservice.kafka.producer;

import com.example.commondto.dto.request.PaymentRequest;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void sendPaymentEvent(PaymentRequest request) {
        log.info("Sending payment request for buyerId={} -> amount={}",
                request.getBuyerId(), request.getAmount());
        kafkaTemplate.send(KafkaTopics.MARKET_PAYMENT_REQUEST, request);
    }
}
