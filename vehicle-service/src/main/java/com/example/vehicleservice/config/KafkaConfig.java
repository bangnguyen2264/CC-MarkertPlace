package com.example.vehicleservice.config;

import com.example.commondto.dto.request.VerifyUpdateRequest;
import com.example.commondto.dto.response.UserValidationResponse;
import com.example.commondto.dto.response.VerifyCreationResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;


    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> clazz) {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.commondto.dto.*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, clazz.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Tạo ConsumerFactory với cấu hình rõ ràng
     */

    @Bean
    public ConsumerFactory<String, UserValidationResponse> userValidationResponseConsumerFactory() {
        return createConsumerFactory(UserValidationResponse.class);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserValidationResponse> userValidationResponseKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserValidationResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userValidationResponseConsumerFactory());
        return factory;
    }
    @Bean
    public ConsumerFactory<String, VerifyCreationResponse> verifyCreationResponseConsumerFactory() {
        return createConsumerFactory(VerifyCreationResponse.class);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VerifyCreationResponse> verifyCreationResponseKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VerifyCreationResponse> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(verifyCreationResponseConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, VerifyUpdateRequest> verifyUpdateRequestConsumerFactory() {
        return createConsumerFactory(VerifyUpdateRequest.class);
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VerifyUpdateRequest> verifyUpdateRequestKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, VerifyUpdateRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(verifyUpdateRequestConsumerFactory());
        return factory;
    }



}