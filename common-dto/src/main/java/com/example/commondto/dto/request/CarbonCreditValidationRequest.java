package com.example.commondto.dto.request;

import jakarta.persistence.Column;
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
public class CarbonCreditValidationRequest extends BaseMessageKafka{

    private String sellerId; // ID người bán (user hoặc tổ chức)

    private String creditId; // ID tín chỉ carbon

    private Double quantity; // số lượng tín chỉ rao bán

}
