package com.payvia.repository;

import com.payvia.entity.RelayPacket;
import com.payvia.entity.RelayPacketState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelayPacketRepository extends JpaRepository<RelayPacket, UUID> {
    Optional<RelayPacket> findByPacketId(UUID packetId);
    Optional<RelayPacket> findByTransactionId(UUID transactionId);
    List<RelayPacket> findByState(RelayPacketState state);
}
