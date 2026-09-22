package com.payvia.repository;

import com.payvia.entity.RelayEdge;
import com.payvia.entity.RelayNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RelayEdgeRepository extends JpaRepository<RelayEdge, UUID> {
    List<RelayEdge> findBySourceNode(RelayNode sourceNode);
    List<RelayEdge> findByTargetNode(RelayNode targetNode);
    boolean existsBySourceNodeIdAndTargetNodeId(UUID sourceNodeId, UUID targetNodeId);
}
