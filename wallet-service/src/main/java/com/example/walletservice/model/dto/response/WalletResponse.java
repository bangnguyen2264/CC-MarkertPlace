package com.example.walletservice.model.dto.response;

import com.example.walletservice.model.entity.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WalletResponse {
    private String id;
    private String ownerId;
    private Double balance;

    public static WalletResponse from(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .ownerId(wallet.getOwnerId())
                .balance(wallet.getBalance())
                .build();
    }
}
