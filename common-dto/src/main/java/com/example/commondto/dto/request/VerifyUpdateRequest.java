package com.example.commondto.dto.request;

import com.example.commondto.constant.Status;
import com.example.commondto.constant.VerifyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class VerifyUpdateRequest extends BaseMessageKafka{
    @NotBlank
    private String userId;
    @NotNull
    private VerifyType type; // VEHICLE, JOURNEY, CREDIT
    @NotBlank
    private String referenceId;
    @NotNull
    private Status status;
    private String note;
}
