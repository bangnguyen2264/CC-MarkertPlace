package com.example.walletservice.service;

import com.example.walletservice.model.dto.response.AuditResponse;
import com.example.walletservice.model.filter.AuditFilter;

import java.util.List;

public interface AuditService {
    List<AuditResponse> getAll(AuditFilter auditFilter);
    AuditResponse getById(String id);
}
