package com.payvia.dto;

import java.util.UUID;

public class RelayNodeDto {
    private UUID id;
    private String nodeName;
    private String nodeType;
    private boolean active;
    private boolean hasInternet;
    private int xPosition;
    private int yPosition;
    private int storedPacketsCount;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isHasInternet() { return hasInternet; }
    public void setHasInternet(boolean hasInternet) { this.hasInternet = hasInternet; }

    public int getxPosition() { return xPosition; }
    public void setxPosition(int xPosition) { this.xPosition = xPosition; }

    public int getyPosition() { return yPosition; }
    public void setyPosition(int yPosition) { this.yPosition = yPosition; }

    public int getStoredPacketsCount() { return storedPacketsCount; }
    public void setStoredPacketsCount(int storedPacketsCount) { this.storedPacketsCount = storedPacketsCount; }
}
