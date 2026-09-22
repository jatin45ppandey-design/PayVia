package com.payvia.repository;

import com.payvia.entity.RelayEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RelayEventRepository extends JpaRepository<RelayEvent, UUID> {
    List<RelayEvent> findByRelayPacketIdOrderByCreatedAtAsc(UUID packetId);
    List<RelayEvent> findByRelayPacketTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
