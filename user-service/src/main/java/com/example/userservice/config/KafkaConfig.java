package com.example.userservice.config;

import com.example.commondto.dto.request.UserValidationRequest;
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
import org.springframework.kafka.listener.DefaultErrorHandler;
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

    /**
     * Tạo ConsumerFactory với cấu hình rõ ràng
     */
    @Bean
    public ConsumerFactory<String, UserValidationRequest> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Cấu hình cho ErrorHandlingDeserializer
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // Cấu hình cho JsonDeserializer
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.commondto.dto.*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, UserValidationRequest.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        log.info("ConsumerFactory created with bootstrap servers: {}", bootstrapServers);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Tạo container factory cho listener
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserValidationRequest>
    kafkaListenerContainerFactory(ConsumerFactory<String, UserValidationRequest> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, UserValidationRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(false);
        factory.setConcurrency(3);

        // Error handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler();
        factory.setCommonErrorHandler(errorHandler);

        log.info("KafkaListenerContainerFactory created for user-service");
        return factory;
    }
}