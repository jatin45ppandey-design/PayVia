package com.payvia.service;

import com.payvia.dto.TransactionDto;
import com.payvia.dto.TransactionTimelineDto;
import com.payvia.entity.*;
import com.payvia.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionTimelineService {

    private final TransactionRepository transactionRepository;
    private final RelayEventRepository relayEventRepository;
    private final SettlementAttemptRepository attemptRepository;
    private final SettlementRepository settlementRepository;

    public TransactionTimelineService(TransactionRepository transactionRepository,
                                      RelayEventRepository relayEventRepository,
                                      SettlementAttemptRepository attemptRepository,
                                      SettlementRepository settlementRepository) {
        this.transactionRepository = transactionRepository;
        this.relayEventRepository = relayEventRepository;
        this.attemptRepository = attemptRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public TransactionTimelineDto getTimeline(UUID transactionId, UUID currentUserId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!transaction.getSender().getId().equals(currentUserId) && !transaction.getReceiver().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Not authorized to view this transaction");
        }

        TransactionTimelineDto dto = new TransactionTimelineDto();
        dto.setTransaction(mapToDto(transaction, currentUserId));

        List<TransactionTimelineDto.EventDto> events = new ArrayList<>();

        // 1. Transaction Created Event
        events.add(new TransactionTimelineDto.EventDto("TRANSACTION_CREATED", "Transaction initiated", "SUCCESS", transaction.getCreatedAt()));

        if (com.payvia.entity.TransactionMode.OFFLINE_RELAY.equals(transaction.getMode())) {
            // 2. Relay Events
            List<RelayEvent> relayEvents = relayEventRepository.findByRelayPacketTransactionIdOrderByCreatedAtAsc(transactionId);
            for (RelayEvent re : relayEvents) {
                events.add(new TransactionTimelineDto.EventDto("RELAY_EVENT", "Relay event: " + re.getEventType().name(), "SUCCESS", re.getCreatedAt()));
            }

            // 3. Settlement Attempts
            List<SettlementAttempt> attempts = attemptRepository.findByTransactionIdOrderByAttemptNumberAsc(transactionId);
            for (SettlementAttempt attempt : attempts) {
                events.add(new TransactionTimelineDto.EventDto("SETTLEMENT_ATTEMPT", "Bridge ingestion attempt " + attempt.getAttemptNumber(), attempt.getStatus(), attempt.getReceivedAt()));
            }

            // 4. Settlement
            settlementRepository.findByTransactionId(transactionId).ifPresent(settlement -> {
                events.add(new TransactionTimelineDto.EventDto("SETTLEMENT", "Final financial settlement", settlement.getStatus(), settlement.getSettledAt()));
            });
        }

        // Sort events by timestamp
        events.sort(Comparator.comparing(TransactionTimelineDto.EventDto::getTimestamp));
        dto.setEvents(events);

        return dto;
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
