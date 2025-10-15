package com.example.userservice.kafka;

import com.example.userservice.config.KafkaConfig;
import com.example.userservice.model.dto.request.UserValidationRequest;
import com.example.userservice.model.dto.response.UserValidationResponse;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidationListener {

    private UserRepository userRepository;

    @KafkaListener(topics = KafkaConfig.REQUEST_TOPIC, groupId = "user-service-group")
    @SendTo(KafkaConfig.REPLY_TOPIC)
    public UserValidationResponse handleValidationRequest(UserValidationRequest request) {
        if (request == null || (request.getUserId() == null && request.getEmail() == null)) {
            return new UserValidationResponse(false, "Invalid request");
        }

        boolean exists = false;
        if (request.getUserId() != null)
            exists = userRepository.existsById(request.getUserId());
        else if (request.getEmail() != null)
            exists = userRepository.existsByEmail(request.getEmail());

        return new UserValidationResponse(exists, exists ? "User found" : "User not found");
    }
}
