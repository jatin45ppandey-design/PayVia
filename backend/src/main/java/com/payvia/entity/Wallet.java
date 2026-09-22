package com.payvia.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "offline_reserved_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal offlineReservedBalance = BigDecimal.ZERO;

    @Column(name = "pending_offline_outgoing", nullable = false, precision = 19, scale = 2)
    private BigDecimal pendingOfflineOutgoing = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getOfflineReservedBalance() { return offlineReservedBalance; }
    public void setOfflineReservedBalance(BigDecimal offlineReservedBalance) { this.offlineReservedBalance = offlineReservedBalance; }

    public BigDecimal getPendingOfflineOutgoing() { return pendingOfflineOutgoing; }
    public void setPendingOfflineOutgoing(BigDecimal pendingOfflineOutgoing) { this.pendingOfflineOutgoing = pendingOfflineOutgoing; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
