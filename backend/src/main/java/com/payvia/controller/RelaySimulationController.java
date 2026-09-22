package com.payvia.controller;

import com.payvia.dto.RelayNetworkDto;
import com.payvia.dto.RelaySimulationStepResultDto;
import com.payvia.entity.RelayNode;
import com.payvia.repository.RelayNodeRepository;
import com.payvia.service.RelaySimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/relay")
public class RelaySimulationController {

    private final RelaySimulationService simulationService;
    private final RelayNodeRepository nodeRepository;
    private final com.payvia.service.BridgeIngestionService bridgeIngestionService;

    public RelaySimulationController(RelaySimulationService simulationService, RelayNodeRepository nodeRepository, com.payvia.service.BridgeIngestionService bridgeIngestionService) {
        this.simulationService = simulationService;
        this.nodeRepository = nodeRepository;
        this.bridgeIngestionService = bridgeIngestionService;
    }

    @GetMapping("/network")
    public ResponseEntity<RelayNetworkDto> getNetwork() {
        return ResponseEntity.ok(simulationService.getNetworkTopology());
    }

    @PostMapping("/network/reset")
    public ResponseEntity<Void> resetNetwork() {
        simulationService.resetNetwork();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/simulation/step")
    public ResponseEntity<RelaySimulationStepResultDto> simulationStep() {
        return ResponseEntity.ok(simulationService.executeSimulationStep());
    }

    @PostMapping("/nodes/{nodeId}/toggle")
    public ResponseEntity<Void> toggleNode(@PathVariable UUID nodeId) {
        RelayNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new IllegalArgumentException("Node not found"));
        node.setActive(!node.isActive());
        nodeRepository.save(node);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/nodes/{nodeId}/internet")
    public ResponseEntity<Void> toggleInternet(@PathVariable UUID nodeId) {
        RelayNode node = nodeRepository.findById(nodeId).orElseThrow(() -> new IllegalArgumentException("Node not found"));
        node.setHasInternet(!node.isHasInternet());
        nodeRepository.save(node);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bridge/flush")
    public ResponseEntity<List<Map<String, String>>> flushBridge() {
        return ResponseEntity.ok(bridgeIngestionService.flushAllBridges());
    }

    @PostMapping("/testing/inject-failure")
    public ResponseEntity<Void> injectFailure() {
        bridgeIngestionService.setSimulateNextFailure(true);
        return ResponseEntity.ok().build();
    }
}
