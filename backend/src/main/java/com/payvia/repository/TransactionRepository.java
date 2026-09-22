package com.payvia.repository;

import com.payvia.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Transaction t WHERE t.id = :id")
    Optional<Transaction> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") UUID id);

    List<Transaction> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(UUID senderId, UUID receiverId);
    
    Optional<Transaction> findByPublicReference(String publicReference);

    long countByMode(com.payvia.entity.TransactionMode mode);
    long countByStatus(com.payvia.entity.TransactionStatus status);
}

// touched to trigger IDE re-indexing
