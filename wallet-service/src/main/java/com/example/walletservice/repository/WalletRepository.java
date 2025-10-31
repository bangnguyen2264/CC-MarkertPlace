package com.example.walletservice.repository;

import com.example.walletservice.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, String>, JpaSpecificationExecutor<Wallet> {
    Optional<Wallet> findByOwnerId(String ownerId);

}
