package com.example.verificationservice.kafka;

import com.example.commondto.dto.request.UserValidationRequest;
import com.example.commondto.dto.request.VerifyCreationRequest;
import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.dto.response.VerifyCreationResponse;
import com.example.commondto.dto.response.WalletCreationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.verificationservice.verify_request.VerifyCreationDto;
import com.example.verificationservice.verify_request.VerifyRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationConsumer {

    private final VerificationProducer verificationProducer;
    private final VerifyRequestService verifyRequestService;

    @KafkaListener(
            topics = KafkaTopics.VERIFY_CREATION_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "verifyRequestCreationKafkaListenerFactory"
    )
    public void consumeUserValidationRequest(VerifyCreationRequest request) {
        log.info("Received verify creation request: {}", request);

        try {
            // Gọi service để tạo verify request
            verifyRequestService.create(convertToVerifyCreationDto(request));

            // Gửi response thành công
            verificationProducer.sendCreateVerifyResponse(VerifyCreationResponse.builder()
                    .success(true)
                    .message("Verify request created successfully")
                    .correlationId(request.getCorrelationId())
                    .build());

        } catch (Exception e) {
            // Log lỗi
            log.error("Error processing verify creation request: {}", request, e);

            // Gửi response lỗi
            verificationProducer.sendCreateVerifyResponse(VerifyCreationResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .correlationId(request.getCorrelationId())
                    .build());
        }
    }

    private VerifyCreationDto convertToVerifyCreationDto(VerifyCreationRequest request) {
        return VerifyCreationDto.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .documentUrl(request.getDocumentUrl())
                .referenceId(request.getReferenceId())
                .userId(request.getUserId())
                .build();
    }
}