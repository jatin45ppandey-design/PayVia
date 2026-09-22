package com.payvia.repository;

import com.payvia.entity.OfflinePaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import com.payvia.entity.Transaction;

@Repository
public interface OfflinePaymentIntentRepository extends JpaRepository<OfflinePaymentIntent, UUID> {
    Optional<OfflinePaymentIntent> findByNonce(UUID nonce);
    boolean existsBySenderDeviceIdAndSequenceNumber(UUID deviceId, Long sequenceNumber);
    Optional<OfflinePaymentIntent> findByTransaction(Transaction transaction);
}