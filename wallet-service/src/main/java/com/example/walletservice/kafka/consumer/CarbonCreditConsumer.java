package com.example.walletservice.kafka.consumer;

import com.example.commondto.dto.request.UpdateCarbonCreditMessage;
import com.example.commondto.dto.request.WalletCreationRequest;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.producer.WalletProducer;
import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.repository.CarbonCreditRepository;
import com.example.walletservice.service.CarbonCreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarbonCreditConsumer {
    private final CarbonCreditRepository carbonCreditRepository;
    private final CarbonCreditService carbonCreditService;


    @KafkaListener(
            topics = KafkaTopics.CC_UPDATE_MESSAGE,
            groupId = "${spring.application.name}-group",
            containerFactory = "ccUpdateKafkaListenerFactory"
    )
    public void consumeWalletCreationRequest(UpdateCarbonCreditMessage message) {
        log.info("Received new update carbon credit request: {}", message);
        CarbonCredit carbonCredit = carbonCreditRepository.findByOwnerId(message.getOwnerId())
                .orElse(null);
        if (carbonCredit == null) {
            throw new NotFoundException("Carbon credit of user with id " + message.getOwnerId() + " not found");
        }
        carbonCreditService.update(
                carbonCredit.getId(),
                CarbonCreditUpdateRequest.builder()
                        .amount(message.getNewTotalCredit())
                        .build()
        );
    }
}
