package com.payvia.repository;

import com.payvia.entity.RelayNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelayNodeRepository extends JpaRepository<RelayNode, UUID> {
    List<RelayNode> findByActiveTrue();
    Optional<RelayNode> findByNodeName(String nodeName);
}
