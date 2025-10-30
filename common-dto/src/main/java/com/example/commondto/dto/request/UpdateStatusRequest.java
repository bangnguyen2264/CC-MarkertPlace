package com.example.commondto.dto.request;

import com.example.commondto.constant.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateStatusRequest {
    @NotNull
    private Status status;
    private String note;
}
