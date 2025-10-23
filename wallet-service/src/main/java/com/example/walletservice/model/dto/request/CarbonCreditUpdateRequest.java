package com.example.walletservice.model.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonCreditUpdateRequest {
    private Double amount;          // Số tín chỉ thay đổi (+ hoặc -)
    private String description;     // Mô tả giao dịch
}

