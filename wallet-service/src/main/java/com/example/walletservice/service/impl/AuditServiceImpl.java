package com.example.walletservice.service.impl;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.walletservice.model.dto.response.AuditResponse;
import com.example.walletservice.model.entity.Audit;
import com.example.walletservice.model.filter.AuditFilter;
import com.example.walletservice.repository.AuditRepository;
import com.example.walletservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditRepository auditRepository;

    @Override
    public void record(String ownerId,
                       TransactionType type,
                       TransactionAction action,
                       Double amount,
                       Double balanceAfter,
                       String description,
                       String referenceId) {

        Audit audit = Audit.builder()
                .ownerId(ownerId)
                .type(type)
                .action(action)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .description(description)
                .referenceId(referenceId)
                .build();

        auditRepository.save(audit);
    }

    @Override
    public List<AuditResponse> getAll(AuditFilter auditFilter) {
        Pageable pageable = CrudUtils.createPageable(auditFilter);
        Specification<Audit> spec = _buildFilter(auditFilter);
        Page<Audit> result = auditRepository.findAll(spec, pageable);
        return result.stream().map(
                AuditResponse::from
        ).collect(Collectors.toList());
    }

    private Specification<Audit> _buildFilter(AuditFilter auditFilter) {
        Specification<Audit> spec = (root, query, cb) -> cb.conjunction();

        if (auditFilter.getOwnerId() != null && !auditFilter.getOwnerId().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("ownerId"), auditFilter.getOwnerId()));
        }
        if (auditFilter.getType() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("type"), auditFilter.getType()));
        }
        if (auditFilter.getAction() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), auditFilter.getAction()));
        }
        if (auditFilter.getReferenceId() != null && !auditFilter.getReferenceId().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("referenceId"), auditFilter.getReferenceId()));
        }
        return spec;
    }

    @Override
    public AuditResponse getById(String id) {
        Audit audit = auditRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Audit not found with id: " + id)
        );
        return AuditResponse.from(audit);
    }
}
