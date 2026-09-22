package com.payvia.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "node_packet_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"node_id", "relay_packet_id"})
})
public class NodePacketStore {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "node_id")
    private RelayNode node;

    @ManyToOne(optional = false)
    @JoinColumn(name = "relay_packet_id")
    private RelayPacket relayPacket;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column
    private OffsetDateTime forwardedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodePacketState state;

    @Column(nullable = false)
    private int hopCount;

    @ManyToOne
    @JoinColumn(name = "previous_node_id")
    private RelayNode previousNode;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RelayNode getNode() { return node; }
    public void setNode(RelayNode node) { this.node = node; }

    public RelayPacket getRelayPacket() { return relayPacket; }
    public void setRelayPacket(RelayPacket relayPacket) { this.relayPacket = relayPacket; }

    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }

    public OffsetDateTime getForwardedAt() { return forwardedAt; }
    public void setForwardedAt(OffsetDateTime forwardedAt) { this.forwardedAt = forwardedAt; }

    public NodePacketState getState() { return state; }
    public void setState(NodePacketState state) { this.state = state; }

    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public RelayNode getPreviousNode() { return previousNode; }
    public void setPreviousNode(RelayNode previousNode) { this.previousNode = previousNode; }
}
