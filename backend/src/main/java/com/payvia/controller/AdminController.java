package com.payvia.controller;

import com.payvia.entity.Role;
import com.payvia.entity.User;
import com.payvia.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final com.payvia.repository.TransactionRepository transactionRepository;
    private final com.payvia.repository.RelayPacketRepository packetRepository;

    public AdminController(UserRepository userRepository,
                           com.payvia.repository.TransactionRepository transactionRepository,
                           com.payvia.repository.RelayPacketRepository packetRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.packetRepository = packetRepository;
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<Map<String, String>> bootstrapAdmin() {
        // Find if there is any admin already
        List<User> admins = userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).toList();
        if (!admins.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists");
        }

        // Make the first user an admin
        User firstUser = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No users found to promote"));

        firstUser.setRole(Role.ADMIN);
        userRepository.save(firstUser);

        return ResponseEntity.ok(Map.of("message", "First user promoted to ADMIN: " + firstUser.getEmail()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        long users = userRepository.count();
        long transactions = transactionRepository.count();
        
        long onlinePayments = transactionRepository.countByMode(com.payvia.entity.TransactionMode.ONLINE);
        long offlinePayments = transactionRepository.countByMode(com.payvia.entity.TransactionMode.OFFLINE_RELAY);
        
        long queued = transactionRepository.countByStatus(com.payvia.entity.TransactionStatus.QUEUED);
        long settled = transactionRepository.countByStatus(com.payvia.entity.TransactionStatus.SETTLED);
        long retryableFailures = transactionRepository.countByStatus(com.payvia.entity.TransactionStatus.FAILED_RETRYABLE);
        long rejected = transactionRepository.countByStatus(com.payvia.entity.TransactionStatus.REJECTED);
        long expired = transactionRepository.countByStatus(com.payvia.entity.TransactionStatus.EXPIRED);
        
        long relayPackets = packetRepository.count();

        Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("users", users);
        metrics.put("wallets", users);
        metrics.put("transactions", transactions);
        metrics.put("onlinePayments", onlinePayments);
        metrics.put("offlinePayments", offlinePayments);
        metrics.put("queued", queued);
        metrics.put("settled", settled);
        metrics.put("retryableFailures", retryableFailures);
        metrics.put("rejected", rejected);
        metrics.put("expired", expired);
        metrics.put("relayPackets", relayPackets);

        return ResponseEntity.ok(metrics);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getAllTransactions() {
        // Warning: Exposing entity directly is bad practice, but since this is an admin dashboard for a prototype, we will return a minimal projection or the entity if it doesn't expose sensitive info.
        // The Transaction entity doesn't have passwords, but it has user references which might loop if we're not careful. Let's return a list of simplified maps.
        List<Map<String, Object>> txs = transactionRepository.findAll().stream().map(t -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", t.getId());
            map.put("reference", t.getPublicReference());
            map.put("mode", t.getMode().name());
            map.put("status", t.getStatus().name());
            map.put("amount", t.getAmount());
            map.put("createdAt", t.getCreatedAt());
            map.put("sender", t.getSender().getPayviaHandle());
            map.put("receiver", t.getReceiver().getPayviaHandle());
            return map;
        }).toList();
        return ResponseEntity.ok(txs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.GetMapping("/network")
    public ResponseEntity<List<Map<String, Object>>> getNetworkMonitor() {
        List<Map<String, Object>> packets = packetRepository.findAll().stream().map(p -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("transactionId", p.getTransaction().getId());
            map.put("state", p.getState().name());
            map.put("hopCount", p.getCurrentHopCount());
            map.put("createdAt", p.getCreatedAt());
            return map;
        }).toList();
        return ResponseEntity.ok(packets);
    }
}
