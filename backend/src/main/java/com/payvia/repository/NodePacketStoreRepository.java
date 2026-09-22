package com.payvia.repository;

import com.payvia.entity.NodePacketState;
import com.payvia.entity.NodePacketStore;
import com.payvia.entity.RelayNode;
import com.payvia.entity.RelayPacket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NodePacketStoreRepository extends JpaRepository<NodePacketStore, UUID> {
    List<NodePacketStore> findByState(NodePacketState state);
    List<NodePacketStore> findByNodeIdAndState(UUID nodeId, NodePacketState state);
    boolean existsByNodeAndRelayPacket(RelayNode node, RelayPacket relayPacket);
    Optional<NodePacketStore> findByNodeAndRelayPacket(RelayNode node, RelayPacket relayPacket);
}
