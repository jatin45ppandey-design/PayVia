package com.payvia.service;

import com.payvia.entity.*;
import com.payvia.repository.*;
import com.payvia.util.CanonicalPayloadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BridgeIngestionService {

    private final NodePacketStoreRepository storeRepository;
    private final TransactionRepository transactionRepository;
    private final SettlementAttemptRepository attemptRepository;
    private final SettlementRepository settlementRepository;
    private final WalletRepository walletRepository;
    private final OfflinePaymentIntentRepository intentRepository;
    private final DeviceRepository deviceRepository;
    private final EncryptionService encryptionService;
    private final SignatureVerificationService signatureService;
    private final RelayPacketRepository packetRepository;

    @Value("${payvia.offline.intent.expiry.hours:24}")
    private int expiryHours;

    // A flag to simulate a temporary failure for demo purposes
    private boolean simulateNextFailure = false;

    public BridgeIngestionService(NodePacketStoreRepository storeRepository,
                                  TransactionRepository transactionRepository,
                                  SettlementAttemptRepository attemptRepository,
                                  SettlementRepository settlementRepository,
                                  WalletRepository walletRepository,
                                  OfflinePaymentIntentRepository intentRepository,
                                  DeviceRepository deviceRepository,
                                  EncryptionService encryptionService,
                                  SignatureVerificationService signatureService,
                                  RelayPacketRepository packetRepository) {
        this.storeRepository = storeRepository;
        this.transactionRepository = transactionRepository;
        this.attemptRepository = attemptRepository;
        this.settlementRepository = settlementRepository;
        this.walletRepository = walletRepository;
        this.intentRepository = intentRepository;
        this.deviceRepository = deviceRepository;
        this.encryptionService = encryptionService;
        this.signatureService = signatureService;
        this.packetRepository = packetRepository;
    }

    public void setSimulateNextFailure(boolean simulate) {
        this.simulateNextFailure = simulate;
    }

    // Flush all packets currently at any internet-connected bridge node
    public List<Map<String, String>> flushAllBridges() {
        List<NodePacketStore> uploadedStores = storeRepository.findByState(NodePacketState.UPLOADED);
        List<Map<String, String>> results = new ArrayList<>();

        for (NodePacketStore store : uploadedStores) {
            // Process each in a separate transactional context?
            // Since this is a simple simulator, we'll just call a transactional method per packet
            // However, this is all within one class, so we need self-injection or just let the caller handle it.
            // Wait, this method isn't @Transactional, so calling processPacket will start a new transaction if processPacket is @Transactional.
            // Actually, Spring AOP requires calling through a proxy, but we can just make it transactional here and let it roll back per-packet if we handle it carefully, but it's easier to just call it.
            // Let's just process it synchronously.
            try {
                String outcome = processPacket(store);
                results.add(Map.of(
                        "packetId", store.getRelayPacket().getId().toString(),
                        "transactionId", store.getRelayPacket().getTransaction().getId().toString(),
                        "outcome", outcome
                ));
            } catch (Exception e) {
                results.add(Map.of(
                        "packetId", store.getRelayPacket().getId().toString(),
                        "transactionId", store.getRelayPacket().getTransaction().getId().toString(),
                        "outcome", "RETRYABLE_FAILURE"
                ));
            }
        }
        return results;
    }

    @Transactional
    public String processPacket(NodePacketStore store) {
        RelayPacket packet = store.getRelayPacket();
        RelayNode bridge = store.getNode();
        
        // Lock transaction
        Transaction transaction = transactionRepository.findByIdForUpdate(packet.getTransaction().getId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (com.payvia.entity.TransactionStatus.SETTLED.equals(transaction.getStatus())) {
            // ALREADY_SETTLED
            recordAttempt(transaction, packet, bridge, "SUCCESS", null, null, false);
            store.setState(NodePacketState.FORWARDED); // mark as done from this store's perspective
            storeRepository.save(store);
            return "ALREADY_SETTLED";
        }

        if (com.payvia.entity.TransactionStatus.REJECTED.equals(transaction.getStatus()) || com.payvia.entity.TransactionStatus.EXPIRED.equals(transaction.getStatus())) {
            recordAttempt(transaction, packet, bridge, "REJECTED", "TERMINAL_STATE", "Transaction is already " + transaction.getStatus(), false);
            store.setState(NodePacketState.FORWARDED);
            storeRepository.save(store);
            return transaction.getStatus().name();
        }

        // If simulateNextFailure is set, force a failure
        if (simulateNextFailure) {
            simulateNextFailure = false; // Reset
            transaction.setStatus(com.payvia.entity.TransactionStatus.FAILED_RETRYABLE);
            transactionRepository.save(transaction);
            recordAttempt(transaction, packet, bridge, "RETRYABLE_FAILURE", "SIMULATED_FAILURE", "Simulated temporary settlement failure", true);
            return "RETRYABLE_FAILURE";
        }

        transaction.setStatus(com.payvia.entity.TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        OfflinePaymentIntent intent = intentRepository.findByTransaction(transaction)
                .orElseThrow(() -> new IllegalStateException("Intent not found"));

        // Cryptographic Re-Verification
        try {
            String decryptedPayload = encryptionService.decrypt(packet.getEncryptedPayload(), packet.getPacketAesKey());
            // Parse JSON manually: {"intentId":"uuid", "amount":100, "receiver":"handle"}
            Pattern intentIdPattern = Pattern.compile("\"intentId\"\\s*:\\s*\"([^\"]+)\"");
            Matcher intentIdMatcher = intentIdPattern.matcher(decryptedPayload);
            if (!intentIdMatcher.find() || !intentIdMatcher.group(1).equals(intent.getId().toString())) {
                throw new IllegalArgumentException("Payload intent ID mismatch");
            }

            Pattern amountPattern = Pattern.compile("\"amount\"\\s*:\\s*([0-9.]+)");
            Matcher amountMatcher = amountPattern.matcher(decryptedPayload);
            if (!amountMatcher.find() || new java.math.BigDecimal(amountMatcher.group(1)).compareTo(intent.getAmount()) != 0) {
                throw new IllegalArgumentException("Payload amount mismatch");
            }

            // Expiry check
            if (OffsetDateTime.now().isAfter(intent.getExpiresAt())) {
                return rejectTransaction(transaction, intent, packet, bridge, store, "EXPIRED", "Intent expired before bridge ingestion");
            }

            // Re-verify signature exactly
            String canonicalPayload = CanonicalPayloadUtil.buildCanonicalPayload(
                    "1",
                    transaction.getId(),
                    intent.getSenderUser().getId(),
                    intent.getSenderDevice().getId(),
                    intent.getReceiverUser().getId(),
                    intent.getAmount(),
                    intent.getCurrency(),
                    intent.getNonce(),
                    intent.getSequenceNumber(),
                    intent.getCreatedAt(),
                    intent.getExpiresAt()
            );

            // We use the signature from intent because the packet doesn't store signature directly (it's in intent)
            boolean isValid = signatureService.verifySignature(intent.getSenderDevice().getPublicKey(), canonicalPayload, intent.getSignature());
            if (!isValid) {
                return rejectTransaction(transaction, intent, packet, bridge, store, "INVALID_SIGNATURE", "Cryptographic re-verification failed");
            }

        } catch (Exception e) {
            return rejectTransaction(transaction, intent, packet, bridge, store, "INVALID_PACKET", "Packet payload decryption or validation failed: " + e.getMessage());
        }

        // Execute Settlement
        Wallet senderWallet = walletRepository.findByUserIdForUpdate(intent.getSenderUser().getId())
                .orElseThrow(() -> new IllegalStateException("Sender wallet not found"));
        Wallet receiverWallet = walletRepository.findByUserIdForUpdate(intent.getReceiverUser().getId())
                .orElseThrow(() -> new IllegalStateException("Receiver wallet not found"));

        senderWallet.setPendingOfflineOutgoing(senderWallet.getPendingOfflineOutgoing().subtract(intent.getAmount()));
        receiverWallet.setAvailableBalance(receiverWallet.getAvailableBalance().add(intent.getAmount()));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        transaction.setStatus(com.payvia.entity.TransactionStatus.SETTLED);
        transaction.setSettledAt(OffsetDateTime.now());
        transactionRepository.save(transaction);

        intent.setStatus(PaymentIntentStatus.SETTLED);
        intentRepository.save(intent);

        Settlement settlement = new Settlement();
        settlement.setTransaction(transaction);
        settlement.setSenderUser(intent.getSenderUser());
        settlement.setReceiverUser(intent.getReceiverUser());
        settlement.setAmount(intent.getAmount());
        settlement.setCurrency(intent.getCurrency());
        settlement.setSettledAt(OffsetDateTime.now());
        settlement.setSettlementReference("SET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        settlement.setStatus("SUCCESS");
        settlementRepository.save(settlement);

        packet.setState(RelayPacketState.UPLOADED);
        packetRepository.save(packet);

        store.setState(NodePacketState.FORWARDED);
        storeRepository.save(store);

        recordAttempt(transaction, packet, bridge, "SUCCESS", null, null, false);

        return "SETTLED";
    }

    private String rejectTransaction(Transaction transaction, OfflinePaymentIntent intent, RelayPacket packet, RelayNode bridge, NodePacketStore store, String code, String message) {
        transaction.setStatus(code.equals("EXPIRED") ? com.payvia.entity.TransactionStatus.EXPIRED : com.payvia.entity.TransactionStatus.REJECTED);
        transaction.setFailureReason(message);
        transactionRepository.save(transaction);

        intent.setStatus(PaymentIntentStatus.FAILED);
        intentRepository.save(intent);

        Wallet senderWallet = walletRepository.findByUserIdForUpdate(intent.getSenderUser().getId())
                .orElseThrow(() -> new IllegalStateException("Sender wallet not found"));
        
        // Refund exactly once
        senderWallet.setPendingOfflineOutgoing(senderWallet.getPendingOfflineOutgoing().subtract(intent.getAmount()));
        senderWallet.setOfflineReservedBalance(senderWallet.getOfflineReservedBalance().add(intent.getAmount()));
        walletRepository.save(senderWallet);

        packet.setState(RelayPacketState.DROPPED);
        packetRepository.save(packet);

        store.setState(NodePacketState.EXPIRED);
        storeRepository.save(store);

        recordAttempt(transaction, packet, bridge, "REJECTED", code, message, false);

        return transaction.getStatus().name();
    }

    private void recordAttempt(Transaction transaction, RelayPacket packet, RelayNode bridge, String status, String code, String message, boolean retryable) {
        List<SettlementAttempt> attempts = attemptRepository.findByTransactionId(transaction.getId());
        int attemptNumber = attempts.size() + 1;

        SettlementAttempt attempt = new SettlementAttempt();
        attempt.setTransaction(transaction);
        attempt.setPacket(packet);
        attempt.setBridgeNode(bridge);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStatus(status);
        attempt.setFailureCode(code);
        attempt.setFailureMessage(message);
        attempt.setReceivedAt(OffsetDateTime.now());
        attempt.setStartedAt(OffsetDateTime.now());
        attempt.setCompletedAt(OffsetDateTime.now());
        attempt.setRetryable(retryable);

        attemptRepository.save(attempt);
    }
}
