package com.payvia.service;

import com.payvia.entity.*;
import com.payvia.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RelaySimulationIntegrationTest {

    @Autowired
    private RelaySimulationService simulationService;

    @Autowired
    private RelayNodeRepository nodeRepository;

    @Autowired
    private RelayPacketRepository packetRepository;

    @Autowired
    private NodePacketStoreRepository storeRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private OfflinePaymentIntentRepository intentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EncryptionService encryptionService;

    private Transaction queuedTx;
    private OfflinePaymentIntent queuedIntent;

    @BeforeEach
    void setup() {
        // Prepare some data
        User sender = new User();
        sender.setFullName("Test Sender");
        sender.setEmail("sender@test.com");
        sender.setPasswordHash("hash");
        sender.setPayviaHandle("sender@payvia");
        sender.setStatus(UserStatus.ACTIVE);
        userRepository.save(sender);

        User receiver = new User();
        receiver.setFullName("Test Receiver");
        receiver.setEmail("receiver@test.com");
        receiver.setPasswordHash("hash");
        receiver.setPayviaHandle("receiver@payvia");
        receiver.setStatus(UserStatus.ACTIVE);
        userRepository.save(receiver);

        Device device = new Device();
        device.setUser(sender);
        device.setDeviceName("My Phone");
        device.setDeviceType(DeviceType.WEB);
        device.setPublicKey("dummy");
        device.setKeyAlgorithm("ECDSA-P256");
        device.setStatus(DeviceStatus.ACTIVE);
        deviceRepository.save(device);

        queuedTx = new Transaction();
        queuedTx.setId(UUID.randomUUID());
        queuedTx.setSender(sender);
        queuedTx.setReceiver(receiver);
        queuedTx.setAmount(new BigDecimal("100.00"));
        queuedTx.setCurrency("INR");
        queuedTx.setMode(TransactionMode.OFFLINE_RELAY);
        queuedTx.setStatus(TransactionStatus.QUEUED);
        queuedTx.setPublicReference("REF123");
        transactionRepository.save(queuedTx);

        queuedIntent = new OfflinePaymentIntent();
        queuedIntent.setTransaction(queuedTx);
        queuedIntent.setSenderDevice(device);
        queuedIntent.setSenderUser(sender);
        queuedIntent.setReceiverUser(receiver);
        queuedIntent.setSequenceNumber(1L);
        queuedIntent.setAmount(new BigDecimal("100.00"));
        queuedIntent.setNonce(UUID.randomUUID());
        queuedIntent.setSignature("dummy_sig");
        queuedIntent.setStatus(PaymentIntentStatus.QUEUED);
        queuedIntent.setExpiresAt(OffsetDateTime.now().plusDays(1));
        intentRepository.save(queuedIntent);

        simulationService.resetNetwork();
    }

    @Test
    void testRelayStartAndEncryption() throws Exception {
        RelayPacket packet = simulationService.startRelay(queuedTx.getId());
        
        assertThat(packet).isNotNull();
        assertThat(packet.getEncryptedPayload()).isNotEqualTo("{\"amount\":100.00}");
        assertThat(packet.getPayloadHash()).isNotBlank();
        
        // Decrypt and check
        String decrypted = encryptionService.decrypt(packet.getEncryptedPayload(), packet.getPacketAesKey());
        assertThat(decrypted).contains("100.0");
        assertThat(decrypted).contains("receiver@payvia");
        
        assertThat(packet.getState()).isEqualTo(RelayPacketState.RELAYING);
        
        Transaction tx = transactionRepository.findById(queuedTx.getId()).get();
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.RELAYING);
    }

    @Test
    void testDuplicateSuppressionAndHopLimits() throws Exception {
        RelayPacket packet = simulationService.startRelay(queuedTx.getId());
        
        RelayNode senderNode = nodeRepository.findByNodeName("Sender").get();
        NodePacketStore store = storeRepository.findByNodeAndRelayPacket(senderNode, packet).get();
        assertThat(store.getState()).isEqualTo(NodePacketState.STORED);
        
        // Step 1: Sender -> Relay A
        simulationService.executeSimulationStep();
        packet = packetRepository.findById(packet.getId()).get();
        assertThat(packet.getCurrentHopCount()).isEqualTo(1);
        
        // Step 2: Relay A -> Relay B, C
        simulationService.executeSimulationStep();
        packet = packetRepository.findById(packet.getId()).get();
        assertThat(packet.getCurrentHopCount()).isEqualTo(2);

        // Step 3: Relay B, C -> Relay D (duplicate suppression should prevent D from storing twice)
        simulationService.executeSimulationStep();
        packet = packetRepository.findById(packet.getId()).get();
        assertThat(packet.getCurrentHopCount()).isEqualTo(3);
        
        RelayNode relayD = nodeRepository.findByNodeName("Relay D").get();
        // D should only have 1 store entry (database unique constraint enforces this as well)
        assertThat(storeRepository.findByNodeAndRelayPacket(relayD, packet)).isPresent();
        
        // Step 4: Relay D -> Bridge
        simulationService.executeSimulationStep();
        packet = packetRepository.findById(packet.getId()).get();
        assertThat(packet.getState()).isEqualTo(RelayPacketState.BRIDGE_REACHED);
    }
}
