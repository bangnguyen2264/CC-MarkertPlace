package com.example.commondto.dto.response;

import com.example.commondto.dto.request.BaseMessageKafka;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentResponse extends BaseMessageKafka {
    private HttpStatus status;
    private boolean success;
    private String message;
}
