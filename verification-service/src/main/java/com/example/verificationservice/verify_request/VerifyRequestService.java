package com.example.verificationservice.verify_request;

import com.example.commondto.constant.Status;
import com.example.commondto.dto.request.UpdateStatusRequest;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.verificationservice.kafka.VerificationProducer;
import com.example.verificationservice.mapper.ConvertHelper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyRequestService {
    private final VerifyRequestRepository verifyRequestRepository;
    private final VerificationProducer verificationProducer;
    private final ConvertHelper convertHelper;

    public VerifyRequest create(VerifyCreationDto verifyCreationDto) {
        VerifyRequest verifyRequest = VerifyRequest.builder()
                .userId(verifyCreationDto.getUserId())
                .referenceId(verifyCreationDto.getReferenceId())
                .type(verifyCreationDto.getType())
                .title(verifyCreationDto.getTitle())
                .description(verifyCreationDto.getDescription())
                .documentUrl(verifyCreationDto.getDocumentUrl())
                .status(Status.PENDING)
                .note(verifyCreationDto.getNote())
                .build();
        return verifyRequestRepository.save(verifyRequest);
    }

    public VerifyRequest getById(String id) {
        return verifyRequestRepository.findById(id).orElseThrow(
                () -> new NotFoundException("VerifyRequest not found")
        );
    }

    public List<VerifyRequest> getAll(VerifyRequestFilter filter) {
        Pageable pageable = CrudUtils.createPageable(filter);
        Specification<VerifyRequest> spec = _buildFilter(filter);
        return verifyRequestRepository.findAll(spec, pageable).getContent();
    }

    private Specification<VerifyRequest> _buildFilter(VerifyRequestFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo userId
            if (filter.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
            }

            // Lọc theo type
            if (filter.getType() != null) {
                predicates.add(cb.equal(root.get("type"), filter.getType()));
            }

            // Lọc theo referenceId
            if (filter.getReferenceId() != null) {
                predicates.add(cb.equal(root.get("referenceId"), filter.getReferenceId()));
            }

            // Lọc theo status
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // ✅ Tìm kiếm theo title (chuỗi chứa trong JSON "data")
            if (filter.getTitle() != null && !filter.getTitle().isBlank()) {
                String pattern = "%" + filter.getTitle().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("data").as(String.class)), pattern));
            }

            // Kết hợp tất cả predicate
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public VerifyRequest update(String id, UpdateStatusRequest request) {
        VerifyRequest verifyRequest = getById(id);
        verifyRequest.setStatus(request.getStatus());
        verifyRequest.setNote(request.getNote());
        log.info("Updating VerifyRequest : {}", verifyRequest);
        verificationProducer.sendUpdateVerifyRequest(convertHelper.toVerifyUpdateRequest(verifyRequest));
        return verifyRequestRepository.save(verifyRequest);
    }
}
