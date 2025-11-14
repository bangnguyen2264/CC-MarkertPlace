package com.example.commondto.dto.request;

import com.example.commondto.constant.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PaymentRequest extends BaseMessageKafka {

    private String buyerId;

    private String sellerId;

    private PaymentMethod method;

    private Double amount;

    private Double credit;

}
