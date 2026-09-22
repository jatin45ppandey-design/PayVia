package com.payvia.repository;

import com.payvia.entity.SettlementAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementAttemptRepository extends JpaRepository<SettlementAttempt, UUID> {
    List<SettlementAttempt> findByTransactionIdOrderByAttemptNumberAsc(UUID transactionId);
    List<SettlementAttempt> findByTransactionId(UUID transactionId);
}
