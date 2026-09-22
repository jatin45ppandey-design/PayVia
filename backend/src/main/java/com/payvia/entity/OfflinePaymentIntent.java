package com.payvia.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "offline_payment_intents")
public class OfflinePaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_device_id", nullable = false)
    private Device senderDevice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiverUser;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(nullable = false, unique = true)
    private UUID nonce;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentIntentStatus status;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public User getSenderUser() { return senderUser; }
    public void setSenderUser(User senderUser) { this.senderUser = senderUser; }

    public Device getSenderDevice() { return senderDevice; }
    public void setSenderDevice(Device senderDevice) { this.senderDevice = senderDevice; }

    public User getReceiverUser() { return receiverUser; }
    public void setReceiverUser(User receiverUser) { this.receiverUser = receiverUser; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public UUID getNonce() { return nonce; }
    public void setNonce(UUID nonce) { this.nonce = nonce; }

    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public PaymentIntentStatus getStatus() { return status; }
    public void setStatus(PaymentIntentStatus status) { this.status = status; }
}