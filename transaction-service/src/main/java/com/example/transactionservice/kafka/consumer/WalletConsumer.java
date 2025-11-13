package com.example.transactionservice.kafka.consumer;

import com.example.commondto.dto.response.PaymentResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.transactionservice.intergration.WalletIntegration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumer {

    private final WalletIntegration walletIntegration;

    @KafkaListener(
            topics = KafkaTopics.WALLET_CREATION_RESPONSE,
            groupId = "${spring.application.name}-group",
            containerFactory = "paymentResponseKafkaListenerFactory"
    )
    public void consumeWalletPaymentResponse(PaymentResponse response) {
        log.info("Received PaymentResponse correlationId={} success={} message={}",
                response.getCorrelationId(), response.isSuccess(), response.getMessage());
        walletIntegration.completeResponse(response);
    }
}
