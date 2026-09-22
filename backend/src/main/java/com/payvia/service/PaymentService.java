package com.payvia.service;

import com.payvia.dto.PaymentRequestDto;
import com.payvia.dto.TransactionDto;
import com.payvia.entity.Transaction;
import com.payvia.entity.User;
import com.payvia.entity.Wallet;
import com.payvia.repository.TransactionRepository;
import com.payvia.repository.UserRepository;
import com.payvia.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public PaymentService(TransactionRepository transactionRepository, WalletRepository walletRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionDto sendMoney(UUID senderId, PaymentRequestDto request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepository.findByPayviaHandle(request.getReceiverHandle())
                .orElseThrow(() -> new IllegalArgumentException("Receiver handle not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send money to yourself");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        // Lock wallets deterministically to avoid deadlocks (order by ID)
        Wallet senderWallet;
        Wallet receiverWallet;

        if (sender.getId().compareTo(receiver.getId()) < 0) {
            senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));
            receiverWallet = walletRepository.findByUserIdForUpdate(receiver.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found"));
        } else {
            receiverWallet = walletRepository.findByUserIdForUpdate(receiver.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver wallet not found"));
            senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));
        }

        if (senderWallet.getAvailableBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient available balance");
        }

        // Debit & Credit
        senderWallet.setAvailableBalance(senderWallet.getAvailableBalance().subtract(request.getAmount()));
        receiverWallet.setAvailableBalance(receiverWallet.getAvailableBalance().add(request.getAmount()));
        
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        // Record Transaction
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setPublicReference("PAYVIA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency("INR");
        transaction.setMode(com.payvia.entity.TransactionMode.ONLINE);
        transaction.setStatus(com.payvia.entity.TransactionStatus.SETTLED);
        transaction.setSettledAt(OffsetDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);

        return mapToDto(savedTransaction, senderId);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getHistory(UUID userId) {
        return transactionRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .map(t -> mapToDto(t, userId))
                .collect(Collectors.toList());
    }

    private TransactionDto mapToDto(Transaction transaction, UUID currentUserId) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setPublicReference(transaction.getPublicReference());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setMode(transaction.getMode().name());
        dto.setStatus(transaction.getStatus().name());
        dto.setDate(transaction.getCreatedAt());

        if (transaction.getSender().getId().equals(currentUserId)) {
            dto.setType("SENT");
            dto.setCounterpartyName(transaction.getReceiver().getFullName());
            dto.setCounterpartyHandle(transaction.getReceiver().getPayviaHandle());
        } else {
            dto.setType("RECEIVED");
            dto.setCounterpartyName(transaction.getSender().getFullName());
            dto.setCounterpartyHandle(transaction.getSender().getPayviaHandle());
        }
        return dto;
    }
}

// touched to trigger IDE re-indexing
