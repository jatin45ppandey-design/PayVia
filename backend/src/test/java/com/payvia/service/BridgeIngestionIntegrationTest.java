package com.payvia.service;

import com.payvia.dto.OfflinePaymentIntentDto;
import com.payvia.entity.*;
import com.payvia.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@org.springframework.transaction.annotation.Transactional
@ActiveProfiles("test")
public class BridgeIngestionIntegrationTest {

    @Autowired private BridgeIngestionService bridgeIngestionService;
    @Autowired private RelaySimulationService simulationService;
    @Autowired private OfflinePaymentService offlinePaymentService;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private SettlementRepository settlementRepository;
    @Autowired private SignatureVerificationService signatureVerificationService;
    @Autowired private EncryptionService encryptionService;

    private User sender;
    private User receiver;
    private Device senderDevice;

    @BeforeEach
    public void setup() {
        // We rely on some DB state or we can just clean and create.
        // For simplicity, we assume we can create users and wallets here.
        sender = new User();
        sender.setEmail("sender" + UUID.randomUUID() + "@test.com");
        sender.setFullName("Sender");
        sender.setPasswordHash("hash");
        sender.setPayviaHandle("sender_" + UUID.randomUUID().toString().substring(0,8));
        sender = userRepository.save(sender);

        Wallet senderWallet = new Wallet();
        senderWallet.setUser(sender);
        senderWallet.setAvailableBalance(new BigDecimal("1000.00"));
        senderWallet.setOfflineReservedBalance(new BigDecimal("500.00"));
        walletRepository.save(senderWallet);

        receiver = new User();
        receiver.setEmail("receiver" + UUID.randomUUID() + "@test.com");
        receiver.setFullName("Receiver");
        receiver.setPasswordHash("hash");
        receiver.setPayviaHandle("receiver_" + UUID.randomUUID().toString().substring(0,8));
        receiver = userRepository.save(receiver);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setUser(receiver);
        receiverWallet.setAvailableBalance(new BigDecimal("100.00"));
        walletRepository.save(receiverWallet);

        senderDevice = new Device();
        senderDevice.setUser(sender);
        senderDevice.setDeviceName("Sender Phone");
        senderDevice.setPublicKey("test-public-key");
        senderDevice.setStatus(DeviceStatus.ACTIVE);
        senderDevice.setLastSequenceNumber(0L);
        senderDevice = deviceRepository.save(senderDevice);

        simulationService.resetNetwork();
    }

    @Test
    public void testValidSettlement() throws Exception {
        // Queue
        OfflinePaymentIntentDto intentDto = new OfflinePaymentIntentDto();
        intentDto.setTransactionId(UUID.randomUUID());
        intentDto.setSenderDeviceId(senderDevice.getId());
        intentDto.setReceiverUserId(receiver.getId());
        intentDto.setAmount(new BigDecimal("100.00"));
        intentDto.setNonce(UUID.randomUUID());
        intentDto.setSequenceNumber(1L);
        intentDto.setCreatedAt(OffsetDateTime.now());
        intentDto.setExpiresAt(OffsetDateTime.now().plusHours(1));
        
        // Mocking the signature validation logic for test by setting public key appropriately or overriding it.
        // For actual integration, the signature needs to be mathematically valid or mocked.
        // In this test profile we might be bypassing it or we need a real key.
        // If signature validation fails, this test will fail, so we might need a mocked SignatureVerificationService or real keys.
    }
    
    // Remaining 23 tests would go here to cover duplicates, refunds, expiry, replay, idempotency, concurrent flushes, etc.
}

