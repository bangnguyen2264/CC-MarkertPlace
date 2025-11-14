package com.example.walletservice.service;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.walletservice.model.dto.response.AuditResponse;
import com.example.walletservice.model.filter.AuditFilter;

import java.util.List;

public interface AuditService {
    public void record(String ownerId,
                       TransactionType type,
                       TransactionAction action,
                       Double amount,
                       Double balanceAfter,
                       String description,
                       String referenceId);
    List<AuditResponse> getAll(AuditFilter auditFilter);
    AuditResponse getById(String id);
}
