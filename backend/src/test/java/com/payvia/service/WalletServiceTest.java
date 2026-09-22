package com.payvia.service;

import com.payvia.dto.WalletDto;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.entity.Wallet;
import com.payvia.repository.UserRepository;
import com.payvia.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setEmail("testwallet@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Test User");
        user.setPayviaHandle("testwallet");
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        walletService.createWalletForUser(user);
    }

    @Test
    void testReserveOfflineFunds_Succeeds() {
        WalletDto wallet = walletService.reserveOfflineFunds(user.getId(), new BigDecimal("2000.00"));
        assertEquals(new BigDecimal("8000.00"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("2000.00"), wallet.getOfflineReservedBalance());
        assertEquals(new BigDecimal("10000.00"), wallet.getTotalBalance()); // preserves total value
    }

    @Test
    void testReserveOfflineFunds_InsufficientFunds_Fails() {
        Exception e = assertThrows(IllegalStateException.class, () -> {
            walletService.reserveOfflineFunds(user.getId(), new BigDecimal("15000.00"));
        });
        assertEquals("Insufficient available balance", e.getMessage());
    }

    @Test
    void testReleaseOfflineFunds_Succeeds() {
        walletService.reserveOfflineFunds(user.getId(), new BigDecimal("2000.00"));
        WalletDto wallet = walletService.releaseOfflineFunds(user.getId(), new BigDecimal("500.00"));
        
        assertEquals(new BigDecimal("8500.00"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("1500.00"), wallet.getOfflineReservedBalance());
        assertEquals(new BigDecimal("10000.00"), wallet.getTotalBalance()); // preserves total value
    }

    @Test
    void testReleaseOfflineFunds_ExcessiveAmount_Fails() {
        walletService.reserveOfflineFunds(user.getId(), new BigDecimal("2000.00"));
        Exception e = assertThrows(IllegalStateException.class, () -> {
            walletService.releaseOfflineFunds(user.getId(), new BigDecimal("2500.00"));
        });
        assertEquals("Insufficient offline reserved balance", e.getMessage());
    }
}
