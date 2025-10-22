package com.example.userservice.kafka.producer;

import com.example.commondto.dto.request.WalletCreationRequest;
import com.example.commondto.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateWalletRequest(WalletCreationRequest request) {
        log.info("Sending wallet creation request: {}", request);
        kafkaTemplate.send(KafkaTopics.WALLET_CREATION_REQUEST, request);
    }
}
