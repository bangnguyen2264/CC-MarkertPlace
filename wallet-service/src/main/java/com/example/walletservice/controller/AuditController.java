package com.example.walletservice.controller;

import com.example.walletservice.model.dto.response.AuditResponse;
import com.example.walletservice.model.filter.AuditFilter;
import com.example.walletservice.service.AuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;


    @GetMapping
    public ResponseEntity<List<AuditResponse>> getAll(@Valid @ParameterObject AuditFilter filter) {
        return ResponseEntity.ok(auditService.getAll(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(auditService.getById(id));
    }
}
