package com.example.walletservice.kafka.consumer;

import com.example.commondto.dto.request.WalletCreationRequest;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.producer.WalletProducer;
import com.example.walletservice.service.WalletService;
import com.example.walletservice.service.CarbonCreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletService walletService;
    private final CarbonCreditService carbonCreditService;
    private final WalletProducer walletProducer;

    @KafkaListener(
            topics = KafkaTopics.WALLET_CREATION_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "walletCreationKafkaListenerFactory"
    )
    public void consumeWalletCreationRequest(WalletCreationRequest request) {
        log.info("Received wallet creation request: {}", request);

        if (request.getOwnerId() == null || request.getOwnerId().isEmpty()) {
            walletProducer.sendCreateWalletResponse(WalletCreationResponse.builder()
                    .ownerId(request.getOwnerId())
                    .success(false)
                    .message("Owner ID is missing")
                    .build());
            return;
        }

        walletService.create(request.getOwnerId());
        carbonCreditService.create(request.getOwnerId());

        walletProducer.sendCreateWalletResponse(WalletCreationResponse.builder()
                .ownerId(request.getOwnerId())
                .success(true)
                .message("Wallet and CarbonCredit created successfully")
                .build());
    }
}
