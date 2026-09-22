package com.payvia.controller;

import com.payvia.repository.AppHealthRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final AppHealthRepository appHealthRepository;

    public HealthController(AppHealthRepository appHealthRepository) {
        this.appHealthRepository = appHealthRepository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("application", "Payvia");
        
        try {
            long count = appHealthRepository.count();
            if (count > 0) {
                response.put("status", "UP");
                response.put("database", "CONNECTED");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "UP");
                response.put("database", "CONNECTED_NO_DATA");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "DISCONNECTED");
            return ResponseEntity.status(503).body(response);
        }
    }
}
