package com.example.commondto.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic userValidationRequestTopic() {
        return new NewTopic(KafkaTopics.USER_VALIDATION_REQUEST, 3, (short) 1);
    }

    @Bean
    public NewTopic userValidationResponseTopic() {
        return new NewTopic(KafkaTopics.USER_VALIDATION_RESPONSE, 3, (short) 1);
    }
}
