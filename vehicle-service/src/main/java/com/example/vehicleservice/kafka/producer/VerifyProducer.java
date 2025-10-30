package com.example.vehicleservice.kafka.producer;

import com.example.commondto.dto.request.UserValidationRequest;
import com.example.commondto.dto.request.VerifyCreationRequest;
import com.example.commondto.exception.BadRequestException;
import com.example.commondto.kafka.KafkaTopics;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Validator validator;
    public void sendCreateVerifyRequest(VerifyCreationRequest verifyCreationRequest) {
        log.info("Sending verification request: {}", verifyCreationRequest);
        Set<ConstraintViolation<VerifyCreationRequest>> violations = validator.validate(verifyCreationRequest);
        if (!violations.isEmpty()) {
            // Tạo thông điệp lỗi từ các vi phạm
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            // Throw ConstraintViolationException với thông điệp và tập hợp vi phạm
            throw new BadRequestException(errorMessage);
        }

        kafkaTemplate.send(KafkaTopics.VERIFY_CREATION_REQUEST, verifyCreationRequest);
    }
}
