package com.example.verificationservice.mapper;

import com.example.commondto.dto.request.VerifyUpdateRequest;
import com.example.verificationservice.verify_request.VerifyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConvertHelper {

    public VerifyUpdateRequest toVerifyUpdateRequest(VerifyRequest verifyRequest) {
        return VerifyUpdateRequest.builder()
                .userId(verifyRequest.getUserId())
                .status(verifyRequest.getStatus())
                .type(verifyRequest.getType())
                .referenceId(verifyRequest.getReferenceId())
                .note(verifyRequest.getNote())
                .build();
    }
}
