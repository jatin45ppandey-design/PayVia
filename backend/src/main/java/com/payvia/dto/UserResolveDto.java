package com.payvia.dto;

import java.util.UUID;

public class UserResolveDto {
    private UUID id;
    private String fullName;
    private String payviaHandle;

    public UserResolveDto(UUID id, String fullName, String payviaHandle) {
        this.id = id;
        this.fullName = fullName;
        this.payviaHandle = payviaHandle;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPayviaHandle() { return payviaHandle; }
    public void setPayviaHandle(String payviaHandle) { this.payviaHandle = payviaHandle; }
}
