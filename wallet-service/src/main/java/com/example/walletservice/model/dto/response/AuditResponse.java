package com.example.walletservice.model.dto.response;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.walletservice.model.entity.Audit;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditResponse {
    private String id;
    private String ownerId;
    private TransactionType type;
    private TransactionAction action;
    private Double amount;
    private Double balanceAfter;
    private String description;
    private String referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public static AuditResponse from(Audit audit) {
        return AuditResponse.builder()
                .id(audit.getId())
                .ownerId(audit.getOwnerId())
                .type(audit.getType())
                .action(audit.getAction())
                .amount(audit.getAmount())
                .balanceAfter(audit.getBalanceAfter())
                .description(audit.getDescription())
                .referenceId(audit.getReferenceId())
                .createdAt(audit.getCreatedAt())
                .updatedAt(audit.getUpdatedAt())
                .build();
    }
}
