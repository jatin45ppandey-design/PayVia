package com.payvia.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "relay_packets")
public class RelayPacket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID packetId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offline_intent_id")
    private OfflinePaymentIntent offlineIntent;

    @Column(nullable = false)
    private String protocolVersion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_device_id")
    private Device senderDevice;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private int hopLimit;

    @Column(nullable = false)
    private int currentHopCount = 0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String encryptedPayload;

    @Column(nullable = false)
    private String payloadHash;

    @Column(nullable = false)
    private String packetAesKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelayPacketState state;

    @ManyToOne
    @JoinColumn(name = "created_by_node_id")
    private RelayNode createdByNode;

    @Column
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPacketId() { return packetId; }
    public void setPacketId(UUID packetId) { this.packetId = packetId; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public OfflinePaymentIntent getOfflineIntent() { return offlineIntent; }
    public void setOfflineIntent(OfflinePaymentIntent offlineIntent) { this.offlineIntent = offlineIntent; }

    public String getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }

    public Device getSenderDevice() { return senderDevice; }
    public void setSenderDevice(Device senderDevice) { this.senderDevice = senderDevice; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getHopLimit() { return hopLimit; }
    public void setHopLimit(int hopLimit) { this.hopLimit = hopLimit; }

    public int getCurrentHopCount() { return currentHopCount; }
    public void setCurrentHopCount(int currentHopCount) { this.currentHopCount = currentHopCount; }

    public String getEncryptedPayload() { return encryptedPayload; }
    public void setEncryptedPayload(String encryptedPayload) { this.encryptedPayload = encryptedPayload; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public String getPacketAesKey() { return packetAesKey; }
    public void setPacketAesKey(String packetAesKey) { this.packetAesKey = packetAesKey; }

    public RelayPacketState getState() { return state; }
    public void setState(RelayPacketState state) { this.state = state; }

    public RelayNode getCreatedByNode() { return createdByNode; }
    public void setCreatedByNode(RelayNode createdByNode) { this.createdByNode = createdByNode; }

    public OffsetDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(OffsetDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
