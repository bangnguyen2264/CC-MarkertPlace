package com.example.walletservice.kafka.consumer;

import com.example.commondto.dto.request.CarbonCreditValidationRequest;
import com.example.commondto.dto.response.CarbonCreditValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.walletservice.kafka.producer.ValidateCarbonCreditProducer;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.repository.CarbonCreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidateCarbonCreditConsumer {

    private final CarbonCreditRepository carbonCreditRepository;
    private final ValidateCarbonCreditProducer validateCarbonCreditProducer;

    @KafkaListener(
            topics = KafkaTopics.CC_VALIDATE_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "carbonCreditValidationKafkaListenerFactory"
    )
    public void consumeCarbonCreditValidateRequest(CarbonCreditValidationRequest request) {
        log.info("📩 Received carbon credit validation request: {}", request);

        try {
            // 1️⃣ Lấy carbon credit theo sellerId
            Optional<CarbonCredit> sellerCreditOpt = carbonCreditRepository.findByOwnerId(request.getSellerId());
            Optional<CarbonCredit> creditOpt = carbonCreditRepository.findById(request.getCreditId());

            if (sellerCreditOpt.isEmpty() || creditOpt.isEmpty()) {
                log.warn("❌ CarbonCredit not found for sellerId={} or creditId={}",
                        request.getSellerId(), request.getCreditId());
                sendAndReturn(false, "CarbonCredit not found", request.getCorrelationId());
                return;
            }

            CarbonCredit sellerCredit = sellerCreditOpt.get();
            CarbonCredit targetCredit = creditOpt.get();

            // 2️⃣ Kiểm tra quyền sở hữu hợp lệ
            if (!sellerCredit.getId().equals(targetCredit.getId())) {
                log.warn("⚠️ CarbonCredit mismatch for sellerId={} and creditId={}",
                        request.getSellerId(), request.getCreditId());
                sendAndReturn(false, "CarbonCredit does not belong to this seller", request.getCorrelationId());
                return;
            }

            // 3️⃣ Kiểm tra số lượng tín chỉ khả dụng
            double available = Optional.ofNullable(sellerCredit.getAvailableCredit()).orElse(0.0);
            double required = Optional.ofNullable(request.getQuantity()).orElse(0.0);

            if (available < required) {
                log.warn("⚠️ Insufficient CarbonCredit: available={}, required={}", available, required);
                sendAndReturn(false, "Insufficient CarbonCredit", request.getCorrelationId());
                return;
            }

            // ✅ Thành công
            log.info("✅ CarbonCredit validation success for sellerId={} creditId={}",
                    request.getSellerId(), request.getCreditId());
            sendAndReturn(true, "Validation successful", request.getCorrelationId());

        } catch (Exception e) {
            log.error("💥 Error while validating CarbonCredit: {}", e.getMessage(), e);
            sendAndReturn(false, "Internal error while validating", request.getCorrelationId());
        }
    }

    private void sendAndReturn(boolean success, String message, String correlationId) {
        CarbonCreditValidationResponse response = CarbonCreditValidationResponse.builder()
                .success(success)
                .message(message)
                .correlationId(correlationId)
                .build();

        validateCarbonCreditProducer.sendValidateCarbonCreditResponse(response);
    }
}
