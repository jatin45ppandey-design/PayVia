package com.payvia.service;

import com.payvia.entity.User;
import com.payvia.entity.Wallet;
import com.payvia.repository.UserRepository;
import com.payvia.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AccountingInvariantIntegrationTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    public void testSystemWideAccountingInvariant() {
        List<Wallet> allWallets = walletRepository.findAll();
        
        BigDecimal initialSystemTotal = allWallets.stream()
            .map(w -> w.getAvailableBalance().add(w.getOfflineReservedBalance()).add(w.getPendingOfflineOutgoing()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Perform some random transactions if we have at least 2 users
        if (allWallets.size() >= 2) {
            Wallet w1 = allWallets.get(0);
            Wallet w2 = allWallets.get(1);

            User sender = userRepository.findById(w1.getUser().getId()).orElseThrow();
            User receiver = userRepository.findById(w2.getUser().getId()).orElseThrow();

            com.payvia.dto.PaymentRequestDto request = new com.payvia.dto.PaymentRequestDto();
            request.setReceiverHandle(receiver.getPayviaHandle());
            request.setAmount(new BigDecimal("10.00"));

            paymentService.sendMoney(sender.getId(), request);
            
            // Refetch wallets
            List<Wallet> updatedWallets = walletRepository.findAll();
            BigDecimal finalSystemTotal = updatedWallets.stream()
                .map(w -> w.getAvailableBalance().add(w.getOfflineReservedBalance()).add(w.getPendingOfflineOutgoing()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(initialSystemTotal).isEqualByComparingTo(finalSystemTotal);
        }
    }
}
