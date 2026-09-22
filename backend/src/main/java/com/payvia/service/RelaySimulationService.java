package com.payvia.service;

import com.payvia.dto.RelayEdgeDto;
import com.payvia.dto.RelayEventDto;
import com.payvia.dto.RelayNetworkDto;
import com.payvia.dto.RelayNodeDto;
import com.payvia.dto.RelaySimulationStepResultDto;
import com.payvia.entity.*;
import com.payvia.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class RelaySimulationService {

    private final RelayNodeRepository nodeRepository;
    private final RelayEdgeRepository edgeRepository;
    private final RelayPacketRepository packetRepository;
    private final NodePacketStoreRepository storeRepository;
    private final RelayEventRepository eventRepository;
    private final OfflinePaymentIntentRepository intentRepository;
    private final TransactionRepository transactionRepository;
    private final EncryptionService encryptionService;

    public RelaySimulationService(RelayNodeRepository nodeRepository,
                                  RelayEdgeRepository edgeRepository,
                                  RelayPacketRepository packetRepository,
                                  NodePacketStoreRepository storeRepository,
                                  RelayEventRepository eventRepository,
                                  OfflinePaymentIntentRepository intentRepository,
                                  TransactionRepository transactionRepository,
                                  EncryptionService encryptionService) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.packetRepository = packetRepository;
        this.storeRepository = storeRepository;
        this.eventRepository = eventRepository;
        this.intentRepository = intentRepository;
        this.transactionRepository = transactionRepository;
        this.encryptionService = encryptionService;
    }

    @Transactional
    public void resetNetwork() {
        // Clear all simulation data
        eventRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
        packetRepository.deleteAllInBatch();
        edgeRepository.deleteAllInBatch();
        nodeRepository.deleteAllInBatch();

        // Create Default Nodes
        RelayNode sender = createNode("Sender", NodeType.SENDER, true, false, 100, 300);
        RelayNode relayA = createNode("Relay A", NodeType.RELAY, true, false, 300, 200);
        RelayNode relayB = createNode("Relay B", NodeType.RELAY, true, false, 500, 100);
        RelayNode relayC = createNode("Relay C", NodeType.RELAY, true, false, 500, 300);
        RelayNode relayD = createNode("Relay D", NodeType.RELAY, true, false, 700, 200);
        RelayNode bridge = createNode("Bridge", NodeType.BRIDGE, true, true, 900, 200);

        // Create Default Edges
        createEdge(sender, relayA);
        createEdge(relayA, relayB);
        createEdge(relayA, relayC);
        createEdge(relayB, relayD);
        createEdge(relayC, relayD);
        createEdge(relayD, bridge);
    }

    @Transactional
    public RelayPacket startRelay(UUID transactionId) throws Exception {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != com.payvia.entity.TransactionStatus.QUEUED) {
            throw new IllegalArgumentException("Transaction is not queued");
        }

        OfflinePaymentIntent intent = intentRepository.findByTransaction(transaction)
                .orElseThrow(() -> new IllegalStateException("Offline intent not found for transaction"));

        if (intent.getStatus() != PaymentIntentStatus.QUEUED) {
            throw new IllegalStateException("Intent is not QUEUED");
        }

        // Verify it hasn't already been relayed
        if (packetRepository.findByTransactionId(transactionId).isPresent()) {
            throw new IllegalStateException("Relay packet already exists for this transaction");
        }

        RelayNode senderNode = nodeRepository.findByNodeName("Sender")
                .orElseThrow(() -> new IllegalStateException("Sender node not found. Reset network first."));

        // Create Payload
        String payloadJson = "{\"intentId\":\"" + intent.getId() + "\", \"amount\":" + intent.getAmount() + ", \"receiver\":\"" + intent.getReceiverUser().getPayviaHandle() + "\"}";
        
        // Encrypt Payload
        String aesKey = encryptionService.generateAesKeyBase64();
        String encryptedPayload = encryptionService.encrypt(payloadJson, aesKey);
        String payloadHash = encryptionService.generateSha256Hash(encryptedPayload);

        // Create Packet
        RelayPacket packet = new RelayPacket();
        packet.setPacketId(UUID.randomUUID());
        packet.setTransaction(transaction);
        packet.setOfflineIntent(intent);
        packet.setProtocolVersion("1.0");
        packet.setSenderDevice(intent.getSenderDevice());
        packet.setExpiresAt(intent.getExpiresAt());
        packet.setHopLimit(6);
        packet.setCurrentHopCount(0);
        packet.setEncryptedPayload(encryptedPayload);
        packet.setPayloadHash(payloadHash);
        packet.setPacketAesKey(aesKey);
        packet.setState(RelayPacketState.RELAYING);
        packet.setCreatedByNode(senderNode);
        
        packet = packetRepository.save(packet);

        // Update Transaction
        transaction.setStatus(com.payvia.entity.TransactionStatus.RELAYING);
        transactionRepository.save(transaction);

        intent.setStatus(PaymentIntentStatus.RELAYING);
        // I will just change transaction status for now.

        // Place in Sender Node
        NodePacketStore store = new NodePacketStore();
        store.setNode(senderNode);
        store.setRelayPacket(packet);
        store.setHopCount(0);
        store.setState(NodePacketState.STORED);
        storeRepository.save(store);

        // Emit Event
        createEvent(packet, senderNode, null, RelayEventType.CREATED, 0);

        return packet;
    }

    private RelayNode createNode(String name, NodeType type, boolean active, boolean internet, int x, int y) {
        RelayNode node = new RelayNode();
        node.setNodeName(name);
        node.setNodeType(type);
        node.setActive(active);
        node.setHasInternet(internet);
        node.setXPosition(x);
        node.setYPosition(y);
        return nodeRepository.save(node);
    }

    private void createEdge(RelayNode source, RelayNode target) {
        RelayEdge edge = new RelayEdge();
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        edgeRepository.save(edge);
    }

    @Transactional(readOnly = true)
    public RelayNetworkDto getNetworkTopology() {
        List<RelayNode> nodes = nodeRepository.findAll();
        List<RelayEdge> edges = edgeRepository.findAll();

        RelayNetworkDto network = new RelayNetworkDto();
        
        network.setNodes(nodes.stream().map(node -> {
            RelayNodeDto dto = new RelayNodeDto();
            dto.setId(node.getId());
            dto.setNodeName(node.getNodeName());
            dto.setNodeType(node.getNodeType().name());
            dto.setActive(node.isActive());
            dto.setHasInternet(node.isHasInternet());
            dto.setxPosition(node.getXPosition());
            dto.setyPosition(node.getYPosition());
            // count stored packets
            long count = storeRepository.findByNodeIdAndState(node.getId(), NodePacketState.STORED).size();
            dto.setStoredPacketsCount((int)count);
            return dto;
        }).collect(Collectors.toList()));

        network.setEdges(edges.stream().map(edge -> {
            RelayEdgeDto dto = new RelayEdgeDto();
            dto.setId(edge.getId());
            dto.setSourceNodeId(edge.getSourceNode().getId());
            dto.setTargetNodeId(edge.getTargetNode().getId());
            return dto;
        }).collect(Collectors.toList()));

        return network;
    }

    @Transactional
    public RelaySimulationStepResultDto executeSimulationStep() {
        List<RelayEvent> eventsGenerated = new ArrayList<>();
        
        List<NodePacketStore> storedPackets = storeRepository.findByState(NodePacketState.STORED);
        
        for (NodePacketStore store : storedPackets) {
            RelayNode node = store.getNode();
            RelayPacket packet = store.getRelayPacket();
                
                // Expiry Check
                if (OffsetDateTime.now().isAfter(packet.getExpiresAt())) {
                    packet.setState(RelayPacketState.EXPIRED);
                    packetRepository.save(packet);
                    store.setState(NodePacketState.EXPIRED);
                    storeRepository.save(store);
                    eventsGenerated.add(createEvent(packet, node, null, RelayEventType.EXPIRED, packet.getCurrentHopCount()));
                    continue;
                }

                // Hop Limit Check
                if (packet.getCurrentHopCount() >= packet.getHopLimit()) {
                    packet.setState(RelayPacketState.DROPPED);
                    packetRepository.save(packet);
                    store.setState(NodePacketState.EXPIRED); // Or dropped if we had it
                    storeRepository.save(store);
                    eventsGenerated.add(createEvent(packet, node, null, RelayEventType.DROPPED, packet.getCurrentHopCount()));
                    continue;
                }

                // Find neighbors
                List<RelayEdge> outboundEdges = edgeRepository.findBySourceNode(node);
                
                boolean forwardedAtLeastOnce = false;
                
                for (RelayEdge edge : outboundEdges) {
                    RelayNode neighbor = edge.getTargetNode();
                    if (!neighbor.isActive()) continue;
                    
                    // Duplicate suppression: does neighbor already have this packet?
                    boolean neighborHasPacket = storeRepository.existsByNodeAndRelayPacket(neighbor, packet);
                    if (!neighborHasPacket) {
                        // Forward to neighbor
                        NodePacketStore newStore = new NodePacketStore();
                        newStore.setNode(neighbor);
                        newStore.setRelayPacket(packet);
                        newStore.setHopCount(packet.getCurrentHopCount() + 1);
                        newStore.setPreviousNode(node);
                        
                        if (neighbor.getNodeType() == NodeType.BRIDGE && neighbor.isHasInternet()) {
                            newStore.setState(NodePacketState.UPLOADED);
                            packet.setState(RelayPacketState.BRIDGE_REACHED);
                            packet.setUploadedAt(OffsetDateTime.now());
                            eventsGenerated.add(createEvent(packet, node, neighbor, RelayEventType.BRIDGE_REACHED, packet.getCurrentHopCount() + 1));
                        } else {
                            newStore.setState(NodePacketState.STORED);
                            eventsGenerated.add(createEvent(packet, node, neighbor, RelayEventType.FORWARDED, packet.getCurrentHopCount() + 1));
                        }
                        
                        storeRepository.save(newStore);
                        forwardedAtLeastOnce = true;
                    }
                }
                
                if (forwardedAtLeastOnce) {
                    store.setState(NodePacketState.FORWARDED);
                    store.setForwardedAt(OffsetDateTime.now());
                    storeRepository.save(store);
                    
                    packet.setCurrentHopCount(packet.getCurrentHopCount() + 1);
                    packetRepository.save(packet);
                }
        }
        
        RelaySimulationStepResultDto result = new RelaySimulationStepResultDto();
        result.setEvents(eventsGenerated.stream().map(this::mapToDto).collect(Collectors.toList()));
        result.setActivePacketsRemaining(storeRepository.findByState(NodePacketState.STORED).size());
        
        return result;
    }

    private RelayEvent createEvent(RelayPacket packet, RelayNode from, RelayNode to, RelayEventType type, int hop) {
        RelayEvent event = new RelayEvent();
        event.setRelayPacket(packet);
        event.setFromNode(from);
        event.setToNode(to);
        event.setEventType(type);
        event.setHopNumber(hop);
        return eventRepository.save(event);
    }
    
    private RelayEventDto mapToDto(RelayEvent event) {
        RelayEventDto dto = new RelayEventDto();
        dto.setId(event.getId());
        dto.setPacketId(event.getRelayPacket().getId());
        dto.setFromNodeId(event.getFromNode() != null ? event.getFromNode().getId() : null);
        dto.setToNodeId(event.getToNode() != null ? event.getToNode().getId() : null);
        dto.setEventType(event.getEventType().name());
        dto.setHopNumber(event.getHopNumber());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
