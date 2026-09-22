package com.payvia.service;

import com.payvia.dto.PaymentRequestDto;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.entity.UserStatus;
import com.payvia.entity.Wallet;
import com.payvia.repository.TransactionRepository;
import com.payvia.repository.UserRepository;
import com.payvia.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User sender;
    private User receiver;

    @BeforeEach
    public void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        sender = new User();
        sender.setFullName("Sender");
        sender.setEmail("sender@example.com");
        sender.setPasswordHash("hash");
        sender.setPayviaHandle("sender@payvia");
        sender.setStatus(UserStatus.ACTIVE);
        sender.setStatus(UserStatus.ACTIVE);
        sender = userRepository.save(sender);

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setAvailableBalance(new BigDecimal("1000.00"));
        walletRepository.save(senderWallet);

        receiver = new User();
        receiver.setFullName("Receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setPasswordHash("hash2");
        receiver.setPayviaHandle("receiver@payvia");
        receiver.setStatus(UserStatus.ACTIVE);
        receiver.setStatus(UserStatus.ACTIVE);
        receiver = userRepository.save(receiver);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setAvailableBalance(new BigDecimal("500.00"));
        walletRepository.save(receiverWallet);
    }

    @Test
    void testSuccessfulTransfer() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("receiver@payvia");
        req.setAmount(new BigDecimal("200.00"));

        paymentService.sendMoney(sender.getId(), req);

        Wallet sWallet = walletRepository.findByUserId(sender.getId()).get();
        Wallet rWallet = walletRepository.findByUserId(receiver.getId()).get();

        assertEquals(new BigDecimal("800.00"), sWallet.getAvailableBalance());
        assertEquals(new BigDecimal("700.00"), rWallet.getAvailableBalance());
        assertEquals(1, transactionRepository.count());
    }

    @Test
    void testInsufficientBalance() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("receiver@payvia");
        req.setAmount(new BigDecimal("2000.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> paymentService.sendMoney(sender.getId(), req));
    }

    @Test
    void testSelfTransfer() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("sender@payvia");
        req.setAmount(new BigDecimal("100.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> paymentService.sendMoney(sender.getId(), req));
    }

    @Test
    void testUnknownReceiver() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("nobody@payvia");
        req.setAmount(new BigDecimal("100.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> paymentService.sendMoney(sender.getId(), req));
    }

    @Test
    void testNegativeAmount() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("receiver@payvia");
        req.setAmount(new BigDecimal("-10.00"));

        Exception e = assertThrows(IllegalArgumentException.class, () -> paymentService.sendMoney(sender.getId(), req));
    }

    @Test
    void testZeroAmount() {
        PaymentRequestDto req = new PaymentRequestDto();
        req.setReceiverHandle("receiver@payvia");
        req.setAmount(BigDecimal.ZERO);

        Exception e = assertThrows(IllegalArgumentException.class, () -> paymentService.sendMoney(sender.getId(), req));
    }
}

// touched to trigger IDE re-indexing


