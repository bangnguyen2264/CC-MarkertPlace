package com.example.commondto.dto.request;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
public abstract class BaseMessageKafka {
    private String correlationId;

    public BaseMessageKafka() {
        if (this.correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        }
    }

    public BaseMessageKafka(String correlationId) {
        this.correlationId = correlationId != null ? correlationId : UUID.randomUUID().toString();

    }
}
