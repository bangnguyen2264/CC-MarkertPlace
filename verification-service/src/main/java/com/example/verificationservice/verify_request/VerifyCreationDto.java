package com.example.verificationservice.verify_request;

import com.example.commondto.constant.VerifyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class VerifyCreationDto {
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
    private String note;

}
