package com.payvia.controller;

import com.payvia.dto.WalletDto;
import com.payvia.security.JwtUtil;
import com.payvia.service.WalletService;
import com.payvia.service.UserService;
import com.payvia.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.payvia.dto.OfflineReserveRequestDto;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;

    public WalletController(WalletService walletService, UserService userService) {
        this.walletService = walletService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<WalletDto> getMyWallet(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        WalletDto walletDto = walletService.getWalletByUserId(user.getId());
        return ResponseEntity.ok(walletDto);
    }

    @PostMapping("/offline/reserve")
    public ResponseEntity<WalletDto> reserveOfflineFunds(Authentication authentication, @Valid @RequestBody OfflineReserveRequestDto request) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        WalletDto walletDto = walletService.reserveOfflineFunds(user.getId(), request.getAmount());
        return ResponseEntity.ok(walletDto);
    }

    @PostMapping("/offline/release")
    public ResponseEntity<WalletDto> releaseOfflineFunds(Authentication authentication, @Valid @RequestBody OfflineReserveRequestDto request) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        WalletDto walletDto = walletService.releaseOfflineFunds(user.getId(), request.getAmount());
        return ResponseEntity.ok(walletDto);
    }
}
