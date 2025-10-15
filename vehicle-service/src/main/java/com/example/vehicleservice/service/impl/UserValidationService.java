package com.example.vehicleservice.service.impl;

import com.example.vehicleservice.model.dto.request.UserValidationRequest;
import com.example.vehicleservice.model.dto.response.UserValidationResponse;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import com.example.vehicleservice.config.KafkaConfig;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class UserValidationService {

    @Autowired
    private ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate;

    public boolean validateUser(Long userId, String email) {
        try {
            // Gửi request đến user-service
            var data = new UserValidationRequest(userId, email);
            ProducerRecord<String, Object> record =
                    new ProducerRecord<>(KafkaConfig.REQUEST_TOPIC, data);

            // Thêm header topic phản hồi
            record.headers().add(new RecordHeader(
                    KafkaHeaders.REPLY_TOPIC,
                    KafkaConfig.REPLY_TOPIC.getBytes(StandardCharsets.UTF_8)));

            // Gửi và chờ phản hồi
            RequestReplyFuture<String, Object, Object> replyFuture =
                    replyingKafkaTemplate.sendAndReceive(record);

            var response = replyFuture.get(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
            UserValidationResponse validationResponse = (UserValidationResponse) response.value();

            return validationResponse.isValid();
        } catch (Exception e) {
            System.err.println("❌ Lỗi xác thực người dùng: " + e.getMessage());
            return false;
        }
    }
}
