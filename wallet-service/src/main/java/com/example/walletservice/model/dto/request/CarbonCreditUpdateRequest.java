package com.example.walletservice.model.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarbonCreditUpdateRequest {

    private Double totalCredit;          // Tổng số tín chỉ khi đồng bộ hành trình
    private Double tradedCredit;            // Số tín chỉ đã giao dịch
    private String description;     // Mô tả giao dịch
}

