package com.example.commondto.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public abstract class BaseMessageKafka {
    @Builder.Default
    private String correlationId = UUID.randomUUID().toString();

    public BaseMessageKafka() {
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }

    public BaseMessageKafka(String correlationId) {
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();

    }
}
