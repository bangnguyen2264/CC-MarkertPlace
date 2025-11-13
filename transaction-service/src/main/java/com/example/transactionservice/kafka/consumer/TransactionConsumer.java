package com.example.transactionservice.kafka.consumer;

import com.example.commondto.dto.request.MarketPurchaseMessage;
import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.transactionservice.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionConsumer {
    private final TransactionService transactionService;

    @KafkaListener(
            topics = KafkaTopics.MARKET_PURCHASE_EVENT,
            groupId = "${spring.application.name}-group",
            containerFactory = "marketPurchaseEventKafkaListenerFactory"
    )
    public void consumeCarbonCreditValidateResponse(MarketPurchaseMessage message) {
        log.info("📥 Received Market purchase message: {}", message);
        transactionService.createPendingTransaction(message);
    }
}
