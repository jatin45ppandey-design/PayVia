package com.payvia.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "relay_nodes")
public class RelayNode {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nodeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType nodeType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean hasInternet = false;

    @Column(nullable = false)
    private int xPosition = 0;

    @Column(nullable = false)
    private int yPosition = 0;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public NodeType getNodeType() { return nodeType; }
    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isHasInternet() { return hasInternet; }
    public void setHasInternet(boolean hasInternet) { this.hasInternet = hasInternet; }

    public int getXPosition() { return xPosition; }
    public void setXPosition(int xPosition) { this.xPosition = xPosition; }

    public int getYPosition() { return yPosition; }
    public void setYPosition(int yPosition) { this.yPosition = yPosition; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
