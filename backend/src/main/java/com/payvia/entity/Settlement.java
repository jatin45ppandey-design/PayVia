package com.payvia.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
public class Settlement {

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
    @JoinColumn(name = "receiver_user_id", nullable = false)
    private User receiverUser;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "settled_at", nullable = false)
    private OffsetDateTime settledAt;

    @Column(name = "settlement_reference", nullable = false)
    private String settlementReference;

    @Column(nullable = false)
    private String status;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }

    public User getSenderUser() { return senderUser; }
    public void setSenderUser(User senderUser) { this.senderUser = senderUser; }

    public User getReceiverUser() { return receiverUser; }
    public void setReceiverUser(User receiverUser) { this.receiverUser = receiverUser; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public OffsetDateTime getSettledAt() { return settledAt; }
    public void setSettledAt(OffsetDateTime settledAt) { this.settledAt = settledAt; }

    public String getSettlementReference() { return settlementReference; }
    public void setSettlementReference(String settlementReference) { this.settlementReference = settlementReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
