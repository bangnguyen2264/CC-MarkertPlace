package com.example.verificationservice.verify_request;

import com.example.commondto.constant.Status;
import com.example.commondto.constant.VerifyType;
import com.example.commondto.dto.filter.BaseFilter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class VerifyRequestFilter extends BaseFilter {
    private String userId;

    private VerifyType type; // VEHICLE, JOURNEY, CREDIT

    private Status status;

    private String referenceId;

    private String title;

    private String description;

}
