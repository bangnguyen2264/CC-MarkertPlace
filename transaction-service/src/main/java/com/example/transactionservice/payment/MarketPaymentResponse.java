package com.example.transactionservice.payment;

import com.example.transactionservice.transaction.Transaction;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPaymentResponse {
    private String transactionId;
    private String status;
    private String paymentUrl;

    public static MarketPaymentResponse from(Transaction tx) {
        return MarketPaymentResponse.builder()
                .transactionId(tx.getId())
                .status(tx.getStatus().name())
                .paymentUrl(tx.getPaymentUrl())
                .build();
    }
}
