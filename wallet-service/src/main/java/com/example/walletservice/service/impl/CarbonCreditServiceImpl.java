package com.example.walletservice.service.impl;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.exception.BadRequestException;
import com.example.commondto.exception.NotFoundException;
import com.example.commondto.utils.CrudUtils;
import com.example.walletservice.model.dto.request.CarbonCreditUpdateRequest;
import com.example.walletservice.model.dto.response.CarbonCreditResponse;
import com.example.walletservice.model.dto.response.WalletResponse;
import com.example.walletservice.model.entity.Audit;
import com.example.walletservice.model.entity.CarbonCredit;
import com.example.walletservice.model.entity.Wallet;
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
import java.util.stream.Collectors;

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

        double amount = request.getAmount();

        // ✅ Nếu amount > 0 → cộng vào totalCredit và availableCredit
        if (amount > 0) {
            carbonCredit.setTotalCredit(carbonCredit.getTotalCredit() + amount);
            carbonCredit.setAvailableCredit(carbonCredit.getAvailableCredit() + amount);
        }
        // ✅ Nếu amount < 0 → cập nhật tradedCredit và giảm availableCredit
        else if (amount < 0) {
            double absAmount = Math.abs(amount);
            if (carbonCredit.getAvailableCredit() < absAmount) {
                throw new BadRequestException("Not enough available credit to trade");
            }
            carbonCredit.setTradedCredit(carbonCredit.getTradedCredit() + absAmount);
            carbonCredit.setAvailableCredit(carbonCredit.getAvailableCredit() - absAmount);
        }

        carbonCreditRepository.save(carbonCredit);
        return CarbonCreditResponse.from(carbonCredit);
    }

}
