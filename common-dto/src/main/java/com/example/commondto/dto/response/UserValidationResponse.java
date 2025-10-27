package com.example.commondto.dto.response;

import com.example.commondto.dto.request.BaseMessageKafka;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserValidationResponse extends BaseMessageKafka {
    private boolean valid;
    private String message;
}
