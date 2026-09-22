package com.payvia.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifySignatureRequestDto {
    @NotBlank
    private String payload;
    @NotBlank
    private String signature;

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
