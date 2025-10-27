package com.example.userservice.kafka.consumer;

import com.example.commondto.dto.request.UserValidationRequest;
import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.kafka.KafkaTopics;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.kafka.producer.UserProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserConsumer {

    private final UserRepository userRepository;
    private final UserProducer userProducer;

    @KafkaListener(
            topics = KafkaTopics.USER_VALIDATION_REQUEST,
            groupId = "${spring.application.name}-group",
            containerFactory = "userValidationKafkaListenerFactory"
    )
    public void consumeUserValidationRequest(UserValidationRequest request) {
        log.info("📩 Received user validation request with correlationId={}: {}", request.getCorrelationId(), request);

        boolean exists = false;
        String message;

        // Nếu có userId thì kiểm tra theo ID
        if (request.getUserId() != null) {
            exists = userRepository.existsById(request.getUserId());
            message = exists
                    ? "User ID exists"
                    : "User ID not found: " + request.getUserId();
        }
        // Nếu có email thì kiểm tra theo email
        else if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            exists = userRepository.existsByEmail(request.getEmail());
            message = exists
                    ? "Email exists"
                    : "Email not found: " + request.getEmail();
        }
        // Nếu cả hai đều null
        else {
            message = "⚠️ No userId or email provided for validation";
        }

        // Tạo phản hồi với correlationId từ request
        UserValidationResponse response = UserValidationResponse.builder()
                .valid(exists)
                .message(message)
                .correlationId(request.getCorrelationId()) // Copy correlationId
                .build();

        log.info("📤 Sending validation result: {}", response);
        userProducer.sendValidationResponse(response);
    }
}
