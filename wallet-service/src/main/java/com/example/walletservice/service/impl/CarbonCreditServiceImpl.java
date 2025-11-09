package com.example.walletservice.service.impl;

import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.dto.response.CarbonCreditResponse;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.model.filter.CarbonCreditFilter;
import com.example.walletservice.repository.AuditRepository;
import com.example.walletservice.repository.CarbonCreditRepository;
import com.example.walletservice.service.CarbonCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CarbonCreditServiceImpl implements CarbonCreditService {

    private final CarbonCreditRepository carbonCreditRepository;
    private final AuditRepository auditRepository;

    @Override
    public List<CarbonCreditResponse> getAll(CarbonCreditFilter filter) {
        Pageable pageable = CrudUtils.createPageable(filter);
        Page<CarbonCredit> result = carbonCreditRepository.findAll(pageable);
        return result.stream().map(CarbonCreditResponse::from).toList();
    }

    @Override
    public CarbonCreditResponse getById(String id) {
        CarbonCredit credit = carbonCreditRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Carbon credit not found with id: " + id)
        );
        return CarbonCreditResponse.from(credit);
    }

    @Override
    public CarbonCreditResponse create(String ownerId) {
        CarbonCredit credit = CarbonCredit.builder()
                .ownerId(ownerId)
                .totalCredit(0.0)
                .availableCredit(0.0)
                .tradedCredit(0.0)
                .build();

        carbonCreditRepository.save(credit);

        return CarbonCreditResponse.from(credit);
    }

    @Override
    public CarbonCreditResponse update(String id, CarbonCreditUpdateRequest request) {
        CarbonCredit carbonCredit = carbonCreditRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("CarbonCredit not found with id: " + id));

        Double newTotal = request.getTotalCredit();

        // Cập nhật totalCredit nếu có và lớn hơn giá trị hiện tại
        if (newTotal != null && (carbonCredit.getTotalCredit() == null || newTotal > carbonCredit.getTotalCredit())) {
            carbonCredit.setTotalCredit(newTotal);
        }

        // Cập nhật tradedCredit
        if (request.getTradedCredit() != null) {
            Double currentTraded = carbonCredit.getTradedCredit() == null ? 0.0 : carbonCredit.getTradedCredit();
            carbonCredit.setTradedCredit(currentTraded + request.getTradedCredit());
        }

        // Cập nhật availableCredit an toàn (không NullPointer)
        Double currentTotal = carbonCredit.getTotalCredit() != null ? carbonCredit.getTotalCredit() : 0.0;
        Double currentTraded = carbonCredit.getTradedCredit() != null ? carbonCredit.getTradedCredit() : 0.0 ;
        carbonCredit.setAvailableCredit(currentTotal - currentTraded);
        carbonCreditRepository.save(carbonCredit);
        return CarbonCreditResponse.from(carbonCredit);
    }


    @Override
    public CarbonCreditResponse getByOwnerId(String ownerId) {
        CarbonCredit carbonCredit = carbonCreditRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new NotFoundException("CarbonCredit not found with user id: " + ownerId));
        return CarbonCreditResponse.from(carbonCredit);
    }

}
