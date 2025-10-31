package com.example.commondto.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCarbonCreditMessage {
    private String ownerId;
    private Double newTotalCredit;

}
