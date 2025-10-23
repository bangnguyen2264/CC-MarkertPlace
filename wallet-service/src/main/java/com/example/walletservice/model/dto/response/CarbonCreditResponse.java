package com.example.walletservice.model.dto.response;

import com.example.walletservice.model.entity.CarbonCredit;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CarbonCreditResponse {
    private String id;
    private Double totalCredit;
    private Double availableCredit;
    private Double tradedCredit;

    public static CarbonCreditResponse from(CarbonCredit carbonCredit) {
        return CarbonCreditResponse.builder()
                .id(carbonCredit.getId())
                .totalCredit(carbonCredit.getTotalCredit())
                .availableCredit(carbonCredit.getAvailableCredit())
                .tradedCredit(carbonCredit.getTradedCredit())
                .build();
    }
}
