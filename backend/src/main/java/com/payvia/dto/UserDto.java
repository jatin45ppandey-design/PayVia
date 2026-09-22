package com.payvia.dto;

import com.payvia.entity.UserStatus;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String fullName;
    private String email;
    private String payviaHandle;
    private UserStatus status;
    private com.payvia.entity.Role role;

    public UserDto(UUID id, String fullName, String email, String payviaHandle, UserStatus status, com.payvia.entity.Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.payviaHandle = payviaHandle;
        this.status = status;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPayviaHandle() { return payviaHandle; }
    public void setPayviaHandle(String payviaHandle) { this.payviaHandle = payviaHandle; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public com.payvia.entity.Role getRole() { return role; }
    public void setRole(com.payvia.entity.Role role) { this.role = role; }
}
