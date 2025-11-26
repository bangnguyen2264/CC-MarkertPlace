package com.example.walletservice.kafka.producer;

import com.example.commondto.dto.response.PaymentResponse;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.consumer.MarketPaymentConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCreateWalletResponse(WalletCreationResponse response) {
        log.info("Sending wallet creation response for ownerId={} -> success={}",
                response.getOwnerId(), response.isSuccess());
        kafkaTemplate.send(KafkaTopics.WALLET_CREATION_RESPONSE, response);
    }

    public void sendMarketPaymentResponse(PaymentResponse response) {
        log.info("Sending payment response");
        kafkaTemplate.send(KafkaTopics.MARKET_PAYMENT_RESPONSE, response);
    }
}
