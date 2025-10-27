package com.example.commondto.dto.request;

import com.example.commondto.constant.VerifyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
 public class VerifyCreationRequest extends BaseMessageKafka {
    @NotBlank
    private String userId;
    @NotNull
    private VerifyType type; // VEHICLE, JOURNEY, CREDIT
    @NotBlank
    private String referenceId;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private List<String> documentUrl;
}
