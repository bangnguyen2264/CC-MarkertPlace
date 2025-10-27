package com.example.commondto.dto.response;

import com.example.commondto.dto.request.BaseMessageKafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VerifyCreationResponse extends BaseMessageKafka {
    private boolean success;
    private String message;
}
