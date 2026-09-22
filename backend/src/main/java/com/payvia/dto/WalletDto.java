package com.payvia.dto;

import java.math.BigDecimal;

public class WalletDto {
    private BigDecimal availableBalance;
    private BigDecimal offlineReservedBalance;
    private BigDecimal pendingOfflineOutgoing;
    private BigDecimal totalBalance;
    private String currency;

    public WalletDto(BigDecimal availableBalance, BigDecimal offlineReservedBalance, BigDecimal pendingOfflineOutgoing, BigDecimal totalBalance, String currency) {
        this.availableBalance = availableBalance;
        this.offlineReservedBalance = offlineReservedBalance;
        this.pendingOfflineOutgoing = pendingOfflineOutgoing;
        this.totalBalance = totalBalance;
        this.currency = currency;
    }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getOfflineReservedBalance() { return offlineReservedBalance; }
    public void setOfflineReservedBalance(BigDecimal offlineReservedBalance) { this.offlineReservedBalance = offlineReservedBalance; }

    public BigDecimal getPendingOfflineOutgoing() { return pendingOfflineOutgoing; }
    public void setPendingOfflineOutgoing(BigDecimal pendingOfflineOutgoing) { this.pendingOfflineOutgoing = pendingOfflineOutgoing; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}

// touched to trigger IDE re-indexing

