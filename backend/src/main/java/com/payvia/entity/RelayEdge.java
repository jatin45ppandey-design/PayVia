package com.payvia.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "relay_edges", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source_node_id", "target_node_id"})
})
public class RelayEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_node_id")
    private RelayNode sourceNode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_node_id")
    private RelayNode targetNode;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RelayNode getSourceNode() { return sourceNode; }
    public void setSourceNode(RelayNode sourceNode) { this.sourceNode = sourceNode; }

    public RelayNode getTargetNode() { return targetNode; }
    public void setTargetNode(RelayNode targetNode) { this.targetNode = targetNode; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
