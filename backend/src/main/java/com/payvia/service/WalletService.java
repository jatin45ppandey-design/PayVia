package com.payvia.service;

import com.payvia.dto.WalletDto;
import com.payvia.entity.User;
import com.payvia.entity.Wallet;
import com.payvia.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    @Value("${payvia.demo.initial-balance:10000.00}")
    private BigDecimal initialDemoBalance;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setAvailableBalance(initialDemoBalance);
        wallet.setOfflineReservedBalance(BigDecimal.ZERO);
        wallet.setCurrency("INR");
        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public WalletDto getWalletByUserId(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));
        
        BigDecimal totalBalance = wallet.getAvailableBalance().add(wallet.getOfflineReservedBalance());
        return new WalletDto(
                wallet.getAvailableBalance(),
                wallet.getOfflineReservedBalance(),
                wallet.getPendingOfflineOutgoing(),
                totalBalance,
                wallet.getCurrency()
        );
    }

    @Transactional
    public WalletDto reserveOfflineFunds(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));
        
        if (wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }
        
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setOfflineReservedBalance(wallet.getOfflineReservedBalance().add(amount));
        
        walletRepository.save(wallet);
        return getWalletByUserId(userId);
    }

    @Transactional
    public WalletDto releaseOfflineFunds(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));
        
        if (wallet.getOfflineReservedBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient offline reserved balance");
        }
        
        wallet.setOfflineReservedBalance(wallet.getOfflineReservedBalance().subtract(amount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        
        walletRepository.save(wallet);
        return getWalletByUserId(userId);
    }
}

// touched to trigger IDE re-indexing
