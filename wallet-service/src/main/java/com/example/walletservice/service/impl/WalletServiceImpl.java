package com.example.walletservice.service.impl;

import com.example.commondto.constant.TransactionAction;
import com.example.commondto.constant.TransactionType;
import com.example.commondto.utils.CrudUtils;
import com.example.walletservice.model.dto.request.WalletUpdateRequest;
import com.example.walletservice.model.dto.response.WalletResponse;
import com.example.walletservice.model.entity.Audit;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.model.filter.WalletFilter;
import com.example.walletservice.repository.AuditRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final AuditRepository auditRepository;
    @Override
    public List<WalletResponse> getAll(WalletFilter walletFilter) {
        Pageable pageable = CrudUtils.createPageable(walletFilter);
        Page<Wallet> result = walletRepository.findAll( pageable);
        return result.stream().map(WalletResponse::from).toList();
    }

    @Override
    public WalletResponse getById(String id) {
        Wallet wallet = walletRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Wallet not found with id: " + id)
        );
        return WalletResponse.from(wallet);
    }

    @Override
    public WalletResponse create(String ownerId) {
        Wallet wallet = Wallet.builder()
                .ownerId(ownerId)
                .build();

        walletRepository.save(wallet);
        return WalletResponse.from(wallet);
    }

    @Override
    @Transactional
    public WalletResponse update(String id, WalletUpdateRequest walletUpdateRequest) {
        Wallet wallet = walletRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Wallet not found with id: " + id)
        );

        // Xác định loại hành động
        TransactionAction action = walletUpdateRequest.getAmount() >= 0 ? TransactionAction.DEPOSIT : TransactionAction.WITHDRAW;

        // Cập nhật số dư ví
        wallet.setBalance(wallet.getBalance() + walletUpdateRequest.getAmount());
        walletRepository.save(wallet);

        // Tạo bản ghi audit
        Audit audit = Audit.builder()
                .ownerId(wallet.getOwnerId())
                .action(action)
                .type(TransactionType.WALLET)
                .amount(walletUpdateRequest.getAmount())
                .balanceAfter(wallet.getBalance())
                .description(walletUpdateRequest.getDescription())
                .referenceId(wallet.getId())
                .build();
        auditRepository.save(audit);

        // Trả về response
        return WalletResponse.from(wallet);
    }
}
