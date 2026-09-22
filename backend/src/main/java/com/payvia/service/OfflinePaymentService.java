package com.payvia.service;

import com.payvia.dto.OfflinePaymentIntentDto;
import com.payvia.entity.*;
import com.payvia.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class OfflinePaymentService {

    private final OfflinePaymentIntentRepository intentRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final SignatureVerificationService signatureService;

    @Value("${payvia.offline.intent.expiry.hours:24}")
    private int expiryHours;

    public OfflinePaymentService(
            OfflinePaymentIntentRepository intentRepository,
            TransactionRepository transactionRepository,
            WalletRepository walletRepository,
            UserRepository userRepository,
            DeviceRepository deviceRepository,
            SignatureVerificationService signatureService) {
        this.intentRepository = intentRepository;
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.deviceRepository = deviceRepository;
        this.signatureService = signatureService;
    }

    @Transactional
    public com.payvia.dto.OfflinePaymentQueueResponseDto queueOfflinePayment(UUID senderId, OfflinePaymentIntentDto request) {
        // Idempotency check 1: Exact match by nonce
        var existingByNonce = intentRepository.findByNonce(request.getNonce());
        if (existingByNonce.isPresent()) {
            OfflinePaymentIntent existing = existingByNonce.get();
            if (existing.getSenderDevice().getId().equals(request.getSenderDeviceId()) &&
                existing.getSequenceNumber().equals(request.getSequenceNumber()) &&
                existing.getAmount().compareTo(request.getAmount()) == 0 &&
                existing.getReceiverUser().getId().equals(request.getReceiverUserId())) {
                return mapToResponseDto(existing);
            } else {
                throw new IllegalArgumentException("Duplicate nonce with tampered intent data");
            }
        }

        User sender = userRepository.findById(senderId).orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        
        // Lock the device for strictly monotonic sequence check
        Device device = deviceRepository.findByIdAndUserIdAndStatusForUpdate(request.getSenderDeviceId(), senderId, DeviceStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Active device not found for sender"));

        if (request.getSequenceNumber() <= device.getLastSequenceNumber()) {
            throw new IllegalArgumentException("Sequence number must be strictly greater than last used");
        }

        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (request.getExpiresAt().isBefore(request.getCreatedAt())) {
            throw new IllegalArgumentException("ExpiresAt must be after CreatedAt");
        }
        if (request.getExpiresAt().isAfter(request.getCreatedAt().plusHours(expiryHours))) {
            throw new IllegalArgumentException("ExpiresAt cannot exceed configured expiry limit");
        }
        if (OffsetDateTime.now().isAfter(request.getExpiresAt())) {
            throw new IllegalArgumentException("Intent is expired");
        }
        
        // Reject if created too far in the future or past (e.g., skew of > 1 hour)
        if (request.getCreatedAt().isAfter(OffsetDateTime.now().plusHours(1)) ||
            request.getCreatedAt().isBefore(OffsetDateTime.now().minusHours(24))) {
            throw new IllegalArgumentException("CreatedAt is outside acceptable time skew window");
        }

        // Verify signature using CanonicalPayloadUtil
        String canonicalPayload = com.payvia.util.CanonicalPayloadUtil.buildCanonicalPayload(
            "1",
            request.getTransactionId(),
            sender.getId(),
            device.getId(),
            receiver.getId(),
            request.getAmount(),
            "INR",
            request.getNonce(),
            request.getSequenceNumber(),
            request.getCreatedAt(),
            request.getExpiresAt()
        );

        boolean isValid = signatureService.verifySignature(device.getPublicKey(), canonicalPayload, request.getSignature());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid signature");
        }

        // Reserve funds
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

        if (senderWallet.getOfflineReservedBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient offline reserved balance");
        }

        senderWallet.setOfflineReservedBalance(senderWallet.getOfflineReservedBalance().subtract(request.getAmount()));
        senderWallet.setPendingOfflineOutgoing(senderWallet.getPendingOfflineOutgoing().add(request.getAmount()));
        walletRepository.save(senderWallet);

        // Update Device
        device.setLastSequenceNumber(request.getSequenceNumber());
        deviceRepository.save(device);

        // Create Transaction
        Transaction transaction = new Transaction();
        transaction.setId(request.getTransactionId());
        transaction.setPublicReference("PAYVIA-OFF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("INR");
        transaction.setMode(com.payvia.entity.TransactionMode.OFFLINE_RELAY);
        transaction.setStatus(com.payvia.entity.TransactionStatus.QUEUED);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Create Intent
        OfflinePaymentIntent intent = new OfflinePaymentIntent();
        intent.setTransaction(transaction);
        intent.setSenderUser(sender);
        intent.setSenderDevice(device);
        intent.setReceiverUser(receiver);
        intent.setAmount(request.getAmount());
        intent.setCurrency("INR");
        intent.setNonce(request.getNonce());
        intent.setSequenceNumber(request.getSequenceNumber());
        intent.setCreatedAt(request.getCreatedAt());
        intent.setExpiresAt(request.getExpiresAt());
        intent.setSignature(request.getSignature());
        intent.setStatus(PaymentIntentStatus.QUEUED);

        OfflinePaymentIntent savedIntent = intentRepository.save(intent);
        
        return mapToResponseDto(savedIntent);
    }

    private com.payvia.dto.OfflinePaymentQueueResponseDto mapToResponseDto(OfflinePaymentIntent intent) {
        com.payvia.dto.OfflinePaymentQueueResponseDto res = new com.payvia.dto.OfflinePaymentQueueResponseDto();
        res.setTransactionId(intent.getTransaction().getId());
        res.setPublicReference(intent.getTransaction().getPublicReference());
        res.setAmount(intent.getAmount());
        res.setReceiverName(intent.getReceiverUser().getFullName());
        res.setReceiverHandle(intent.getReceiverUser().getPayviaHandle());
        res.setMode(intent.getTransaction().getMode().name());
        res.setStatus(intent.getTransaction().getStatus().name());
        res.setCreatedAt(intent.getCreatedAt());
        res.setExpiresAt(intent.getExpiresAt());
        return res;
    }
}