package com.payvia.controller;

import com.payvia.dto.DeviceDto;
import com.payvia.dto.DeviceRegistrationRequestDto;
import com.payvia.entity.User;
import com.payvia.service.DeviceService;
import com.payvia.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final UserService userService;

    public DeviceController(DeviceService deviceService, UserService userService) {
        this.deviceService = deviceService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<DeviceDto> registerDevice(Authentication authentication, @Valid @RequestBody DeviceRegistrationRequestDto request) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(deviceService.registerDevice(user, request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceDto>> getDevices(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(deviceService.getUserDevices(user.getId()));
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<DeviceDto> revokeDevice(Authentication authentication, @PathVariable UUID id) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(deviceService.revokeDevice(user.getId(), id));
    }

    @PostMapping("/{id}/verify-signature")
    public ResponseEntity<com.payvia.dto.VerifySignatureResponseDto> verifySignature(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody com.payvia.dto.VerifySignatureRequestDto request) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean isValid = deviceService.verifySignature(user.getId(), id, request.getPayload(), request.getSignature());
        return ResponseEntity.ok(new com.payvia.dto.VerifySignatureResponseDto(isValid));
    }
}