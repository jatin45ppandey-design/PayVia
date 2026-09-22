package com.payvia.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OfflinePaymentIntentDto {
    @NotBlank
    private String version;
    @NotNull
    private UUID transactionId;
    @NotNull
    private UUID senderDeviceId;
    @NotNull
    private UUID receiverUserId;
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotNull
    private UUID nonce;
    @NotNull
    private Long sequenceNumber;
    @NotNull
    private OffsetDateTime createdAt;
    @NotNull
    private OffsetDateTime expiresAt;
    @NotBlank
    private String signature;

    // Getters and Setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getSenderDeviceId() { return senderDeviceId; }
    public void setSenderDeviceId(UUID senderDeviceId) { this.senderDeviceId = senderDeviceId; }

    public UUID getReceiverUserId() { return receiverUserId; }
    public void setReceiverUserId(UUID receiverUserId) { this.receiverUserId = receiverUserId; }

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
}