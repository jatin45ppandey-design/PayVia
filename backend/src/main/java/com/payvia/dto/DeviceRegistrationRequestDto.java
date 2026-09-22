package com.payvia.dto;

import jakarta.validation.constraints.NotBlank;

public class DeviceRegistrationRequestDto {
    @NotBlank
    private String deviceName;
    @NotBlank
    private String publicKey;
    @NotBlank
    private String keyAlgorithm; // e.g., "ECDSA-P256"

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    
    public String getKeyAlgorithm() { return keyAlgorithm; }
    public void setKeyAlgorithm(String keyAlgorithm) { this.keyAlgorithm = keyAlgorithm; }
}