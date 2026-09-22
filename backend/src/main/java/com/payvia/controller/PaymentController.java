package com.payvia.controller;

import com.payvia.dto.OfflinePaymentIntentDto;
import com.payvia.dto.PaymentRequestDto;
import com.payvia.dto.TransactionDto;
import com.payvia.entity.OfflinePaymentIntent;
import com.payvia.entity.User;
import com.payvia.service.PaymentService;
import com.payvia.service.UserService;
import com.payvia.service.OfflinePaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final OfflinePaymentService offlinePaymentService;
    private final com.payvia.service.RelaySimulationService relaySimulationService;
    private final com.payvia.service.TransactionTimelineService timelineService;

    public PaymentController(PaymentService paymentService, UserService userService, OfflinePaymentService offlinePaymentService, com.payvia.service.RelaySimulationService relaySimulationService, com.payvia.service.TransactionTimelineService timelineService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.offlinePaymentService = offlinePaymentService;
        this.relaySimulationService = relaySimulationService;
        this.timelineService = timelineService;
    }

    @PostMapping("/send")
    public ResponseEntity<TransactionDto> sendMoney(@Valid @RequestBody PaymentRequestDto request, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        TransactionDto result = paymentService.sendMoney(user.getId(), request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionDto>> getHistory(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<TransactionDto> history = paymentService.getHistory(user.getId());
        return ResponseEntity.ok(history);
    }

    @PostMapping("/offline/queue")
    public ResponseEntity<com.payvia.dto.OfflinePaymentQueueResponseDto> queueOfflinePayment(Authentication authentication, @Valid @RequestBody OfflinePaymentIntentDto request) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        com.payvia.dto.OfflinePaymentQueueResponseDto intent = offlinePaymentService.queueOfflinePayment(user.getId(), request);
        return ResponseEntity.ok(intent);
    }

    @PostMapping("/{transactionId}/relay")
    public ResponseEntity<?> startRelay(Authentication authentication, @PathVariable UUID transactionId) {
        String email = authentication.getName();
        try {
            relaySimulationService.startRelay(transactionId);
            return ResponseEntity.ok().build();
        } catch(Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{transactionId}/timeline")
    public ResponseEntity<com.payvia.dto.TransactionTimelineDto> getTimeline(Authentication authentication, @PathVariable UUID transactionId) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(timelineService.getTimeline(transactionId, user.getId()));
    }
}

// touched to trigger IDE re-indexing
