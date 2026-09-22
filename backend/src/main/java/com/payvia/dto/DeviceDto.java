package com.payvia.dto;

import com.payvia.entity.DeviceStatus;
import com.payvia.entity.DeviceType;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DeviceDto {
    private UUID id;
    private String deviceName;
    private DeviceType deviceType;
    private DeviceStatus status;
    private OffsetDateTime registeredAt;

    public DeviceDto(UUID id, String deviceName, DeviceType deviceType, DeviceStatus status, OffsetDateTime registeredAt) {
        this.id = id;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public UUID getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public DeviceType getDeviceType() { return deviceType; }
    public DeviceStatus getStatus() { return status; }
    public OffsetDateTime getRegisteredAt() { return registeredAt; }
}