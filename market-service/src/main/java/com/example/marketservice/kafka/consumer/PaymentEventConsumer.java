package com.example.marketservice.kafka.consumer;

import com.example.commondto.constant.ListingStatus;
import com.example.commondto.dto.request.MarketPaymentMessage;
import com.example.commondto.dto.request.PaymentRequest;
import com.example.commondto.kafka.KafkaTopics;
import com.example.marketservice.repository.MarketListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {
    private final MarketListingRepository marketListingRepository;

    @KafkaListener(
            topics = KafkaTopics.MARKET_PAYMENT_EVENT,
            groupId = "${spring.application.name}-group",
            containerFactory = "paymentEventKafkaListenerFactory"
    )
    public void consumePaymentEvent(String listingId) {
        marketListingRepository.findById(listingId).ifPresent(marketListing -> {
            marketListing.setStatus(ListingStatus.SOLD);
            marketListingRepository.save(marketListing);
        });
    }
}
