package com.payvia.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "relay_events")
public class RelayEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "packet_id")
    private RelayPacket relayPacket;

    @ManyToOne
    @JoinColumn(name = "from_node_id")
    private RelayNode fromNode;

    @ManyToOne
    @JoinColumn(name = "to_node_id")
    private RelayNode toNode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelayEventType eventType;

    @Column(nullable = false)
    private int hopNumber;

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

    public RelayPacket getRelayPacket() { return relayPacket; }
    public void setRelayPacket(RelayPacket relayPacket) { this.relayPacket = relayPacket; }

    public RelayNode getFromNode() { return fromNode; }
    public void setFromNode(RelayNode fromNode) { this.fromNode = fromNode; }

    public RelayNode getToNode() { return toNode; }
    public void setToNode(RelayNode toNode) { this.toNode = toNode; }

    public RelayEventType getEventType() { return eventType; }
    public void setEventType(RelayEventType eventType) { this.eventType = eventType; }

    public int getHopNumber() { return hopNumber; }
    public void setHopNumber(int hopNumber) { this.hopNumber = hopNumber; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
