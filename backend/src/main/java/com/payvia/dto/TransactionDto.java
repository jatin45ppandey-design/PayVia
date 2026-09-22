package com.payvia.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionDto {
    private java.util.UUID id;
    private String publicReference;
    private String counterpartyName;
    private String counterpartyHandle;
    private String type; // SENT, RECEIVED
    private BigDecimal amount;
    private String currency;
    private String mode;
    private String status;
    private OffsetDateTime date;

    // Getters and setters
    public java.util.UUID getId() { return id; }
    public void setId(java.util.UUID id) { this.id = id; }

    public String getPublicReference() { return publicReference; }
    public void setPublicReference(String publicReference) { this.publicReference = publicReference; }

    public String getCounterpartyName() { return counterpartyName; }
    public void setCounterpartyName(String counterpartyName) { this.counterpartyName = counterpartyName; }

    public String getCounterpartyHandle() { return counterpartyHandle; }
    public void setCounterpartyHandle(String counterpartyHandle) { this.counterpartyHandle = counterpartyHandle; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public OffsetDateTime getDate() { return date; }
    public void setDate(OffsetDateTime date) { this.date = date; }
}
