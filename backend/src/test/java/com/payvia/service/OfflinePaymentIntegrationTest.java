package com.payvia.service;

import com.payvia.dto.DeviceRegistrationRequestDto;
import com.payvia.dto.OfflinePaymentIntentDto;
import com.payvia.entity.Device;
import com.payvia.entity.DeviceStatus;
import com.payvia.entity.User;
import com.payvia.entity.UserStatus;
import com.payvia.repository.DeviceRepository;
import com.payvia.repository.OfflinePaymentIntentRepository;
import com.payvia.repository.UserRepository;
import com.payvia.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class OfflinePaymentIntegrationTest {

    @Autowired
    private OfflinePaymentService offlinePaymentService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OfflinePaymentIntentRepository intentRepository;

    private User sender;
    private User receiver;
    private KeyPair keyPair;
    private Device device;

    @BeforeEach
    public void setup() throws Exception {
        sender = new User();
        sender.setEmail("sender@example.com");
        sender.setPasswordHash("hash");
        sender.setFullName("Sender");
        sender.setPayviaHandle("sender");
        sender.setStatus(UserStatus.ACTIVE);
        userRepository.save(sender);
        walletService.createWalletForUser(sender);

        receiver = new User();
        receiver.setEmail("receiver@example.com");
        receiver.setPasswordHash("hash");
        receiver.setFullName("Receiver");
        receiver.setPayviaHandle("receiver");
        receiver.setStatus(UserStatus.ACTIVE);
        userRepository.save(receiver);
        walletService.createWalletForUser(receiver);

        // Generate ECDSA KeyPair for test
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(256);
        keyPair = kpg.generateKeyPair();
        String pubKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

        DeviceRegistrationRequestDto req = new DeviceRegistrationRequestDto();
        req.setDeviceName("Test Browser");
        req.setKeyAlgorithm("ECDSA-P256");
        req.setPublicKey(pubKeyBase64);
        var dto = deviceService.registerDevice(sender, req);
        device = deviceRepository.findById(dto.getId()).get();

        // Reserve funds
        walletService.reserveOfflineFunds(sender.getId(), new BigDecimal("2000.00"));
    }

    private String sign(String payload, KeyPair kp) throws Exception {
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
        ecdsaSign.initSign(kp.getPrivate());
        ecdsaSign.update(payload.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(ecdsaSign.sign());
    }

    @Test
    void testOfflinePayment_Success() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID nonce = UUID.randomUUID();
        Long seq = 1L;
        OffsetDateTime created = OffsetDateTime.now();
        OffsetDateTime expires = created.plusHours(24);
        BigDecimal amount = new BigDecimal("300.00");

        String payload = com.payvia.util.CanonicalPayloadUtil.buildCanonicalPayload(
            "1", txId, sender.getId(), device.getId(), receiver.getId(),
            amount, "INR", nonce, seq, created, expires
        );
        String signature = sign(payload, keyPair);

        OfflinePaymentIntentDto intentDto = new OfflinePaymentIntentDto();
        intentDto.setVersion("1");
        intentDto.setTransactionId(txId);
        intentDto.setSenderDeviceId(device.getId());
        intentDto.setReceiverUserId(receiver.getId());
        intentDto.setAmount(amount);
        intentDto.setCurrency("INR");
        intentDto.setNonce(nonce);
        intentDto.setSequenceNumber(seq);
        intentDto.setCreatedAt(created);
        intentDto.setExpiresAt(expires);
        intentDto.setSignature(signature);

        var savedIntent = offlinePaymentService.queueOfflinePayment(sender.getId(), intentDto);
        assertEquals("QUEUED", savedIntent.getStatus());

        var wallet = walletRepository.findByUserId(sender.getId()).get();
        assertEquals(new BigDecimal("1700.00"), wallet.getOfflineReservedBalance());

        var receiverWallet = walletRepository.findByUserId(receiver.getId()).get();
        assertEquals(new BigDecimal("10000.00"), receiverWallet.getAvailableBalance()); // unchanged
    }

    @Test
    void testOfflinePayment_TamperedAmount_Fails() throws Exception {
        UUID txId = UUID.randomUUID();
        UUID nonce = UUID.randomUUID();
        Long seq = 1L;
        OffsetDateTime created = OffsetDateTime.now();
        OffsetDateTime expires = created.plusHours(24);
        BigDecimal amount = new BigDecimal("300.00");

        String payload = com.payvia.util.CanonicalPayloadUtil.buildCanonicalPayload(
            "1", txId, sender.getId(), device.getId(), receiver.getId(),
            amount, "INR", nonce, seq, created, expires
        );
        String signature = sign(payload, keyPair);

        OfflinePaymentIntentDto intentDto = new OfflinePaymentIntentDto();
        intentDto.setVersion("1");
        intentDto.setTransactionId(txId);
        intentDto.setSenderDeviceId(device.getId());
        intentDto.setReceiverUserId(receiver.getId());
        intentDto.setAmount(new BigDecimal("500.00")); // TAMPERED AMOUNT
        intentDto.setCurrency("INR");
        intentDto.setNonce(nonce);
        intentDto.setSequenceNumber(seq);
        intentDto.setCreatedAt(created);
        intentDto.setExpiresAt(expires);
        intentDto.setSignature(signature);

        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            offlinePaymentService.queueOfflinePayment(sender.getId(), intentDto);
        });
        assertEquals("Invalid signature", e.getMessage());
    }
}
