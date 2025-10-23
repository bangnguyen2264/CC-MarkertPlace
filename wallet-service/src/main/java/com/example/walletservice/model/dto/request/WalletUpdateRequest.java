package com.example.walletservice.model.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletUpdateRequest {
    private Double amount;       // Số tiền nạp (+) hoặc rút (-)
    private String description;  // Mô tả giao dịch
}
