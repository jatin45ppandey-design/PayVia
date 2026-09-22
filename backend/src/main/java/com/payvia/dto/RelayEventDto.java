package com.payvia.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class RelayEventDto {
    private UUID id;
    private UUID packetId;
    private UUID fromNodeId;
    private UUID toNodeId;
    private String eventType;
    private int hopNumber;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPacketId() { return packetId; }
    public void setPacketId(UUID packetId) { this.packetId = packetId; }

    public UUID getFromNodeId() { return fromNodeId; }
    public void setFromNodeId(UUID fromNodeId) { this.fromNodeId = fromNodeId; }

    public UUID getToNodeId() { return toNodeId; }
    public void setToNodeId(UUID toNodeId) { this.toNodeId = toNodeId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getHopNumber() { return hopNumber; }
    public void setHopNumber(int hopNumber) { this.hopNumber = hopNumber; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
