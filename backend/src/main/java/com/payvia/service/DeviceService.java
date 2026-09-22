package com.payvia.service;

import com.payvia.dto.DeviceDto;
import com.payvia.dto.DeviceRegistrationRequestDto;
import com.payvia.entity.Device;
import com.payvia.entity.DeviceStatus;
import com.payvia.entity.DeviceType;
import com.payvia.entity.User;
import com.payvia.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SignatureVerificationService signatureService;

    public DeviceService(DeviceRepository deviceRepository, SignatureVerificationService signatureService) {
        this.deviceRepository = deviceRepository;
        this.signatureService = signatureService;
    }

    @Transactional
    public DeviceDto registerDevice(User user, DeviceRegistrationRequestDto request) {
        signatureService.validatePublicKeyFormat(request.getPublicKey());
        
        Device device = new Device();
        device.setUser(user);
        device.setDeviceName(request.getDeviceName());
        device.setDeviceType(DeviceType.WEB);
        device.setPublicKey(request.getPublicKey());
        device.setKeyAlgorithm("ECDSA-P256"); // Force strictly this algorithm
        device.setStatus(DeviceStatus.ACTIVE);
        
        Device saved = deviceRepository.save(device);
        
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> getUserDevices(UUID userId) {
        return deviceRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceDto revokeDevice(UUID userId, UUID deviceId) {
        Device device = deviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        
        device.setStatus(DeviceStatus.REVOKED);
        Device saved = deviceRepository.save(device);
        
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public boolean verifySignature(UUID userId, UUID deviceId, String payload, String signature) {
        Device device = deviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        
        if (device.getStatus() != DeviceStatus.ACTIVE) {
            throw new IllegalArgumentException("Device is not active");
        }

        return signatureService.verifySignature(device.getPublicKey(), payload, signature);
    }

    private DeviceDto mapToDto(Device device) {
        return new DeviceDto(
                device.getId(),
                device.getDeviceName(),
                device.getDeviceType(),
                device.getStatus(),
                device.getRegisteredAt()
        );
    }
}