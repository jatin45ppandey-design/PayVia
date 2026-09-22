package com.payvia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequestDto {
    @NotBlank
    private String receiverHandle;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    public String getReceiverHandle() { return receiverHandle; }
    public void setReceiverHandle(String receiverHandle) { this.receiverHandle = receiverHandle; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
