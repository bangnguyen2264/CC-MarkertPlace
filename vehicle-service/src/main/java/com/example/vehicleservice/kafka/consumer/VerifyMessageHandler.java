package com.example.vehicleservice.kafka.consumer;

import com.example.commondto.dto.request.UpdateStatusRequest;
import com.example.commondto.dto.request.VerifyUpdateRequest;
import com.example.commondto.kafka.KafkaTopics;
import com.example.vehicleservice.service.impl.JourneyServiceImpl;
import com.example.vehicleservice.service.impl.VehicleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifyMessageHandler {
    private final VehicleServiceImpl vehicleService;
    private final JourneyServiceImpl journeyService;


    @KafkaListener(
            topics = KafkaTopics.VERIFY_UPDATE_MESSAGE,
            groupId = "${spring.application.name}-group",
            containerFactory = "verifyUpdateRequestKafkaListenerFactory"
    )
    public void consumeVerifyUpdateMessage(VerifyUpdateRequest request) {
        log.info("Received verify update message. type={}, referenceId={}, status={}",
                request.getType(), request.getReferenceId(), request.getStatus());

        UpdateStatusRequest update = UpdateStatusRequest.builder()
                .status(request.getStatus())
                .note(request.getNote())
                .build();

        switch (request.getType()) {
            case VEHICLE -> vehicleService.update(request.getReferenceId(), update);
            case JOURNEY -> journeyService.updateJourneyHistory(request.getReferenceId(), update);
            default -> log.warn("Unknown verify type: {}", request.getType());
        }
    }
}
