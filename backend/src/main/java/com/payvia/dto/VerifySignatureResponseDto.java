package com.payvia.dto;

public class VerifySignatureResponseDto {
    private boolean valid;

    public VerifySignatureResponseDto() {}

    public VerifySignatureResponseDto(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
