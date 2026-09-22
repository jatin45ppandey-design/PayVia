package com.payvia.dto;

import java.util.UUID;

public class RelayEdgeDto {
    private UUID id;
    private UUID sourceNodeId;
    private UUID targetNodeId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(UUID sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public UUID getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(UUID targetNodeId) { this.targetNodeId = targetNodeId; }
}
