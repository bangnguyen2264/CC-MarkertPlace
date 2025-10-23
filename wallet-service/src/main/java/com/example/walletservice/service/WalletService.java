package com.example.walletservice.service;

import com.example.walletservice.model.dto.request.WalletUpdateRequest;
import com.example.walletservice.model.dto.response.WalletResponse;
import com.example.walletservice.model.entity.Wallet;
import com.example.walletservice.model.filter.WalletFilter;

import java.util.List;

public interface WalletService {
    List<WalletResponse> getAll(WalletFilter walletFilter);
    WalletResponse getById(String id);
    WalletResponse create(String ownerId);
    WalletResponse update(String id, WalletUpdateRequest walletUpdateRequest);

}
