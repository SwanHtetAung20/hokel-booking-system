package com.sha.client_service.kafka;

import client.events.ClientEvent;
import com.sha.client_service.domain.Client;
import com.sha.client_service.util.enums.ClientEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendEvent(Client client) {
        ClientEvent clientEvent = ClientEvent.newBuilder()
                .setClientId(client.getId().toString())
                .setName(client.getName())
                .setEmail(client.getEmail())
                .setEventType(ClientEventType.CREATED.getType())
                .build();
        try {
            kafkaTemplate.send("client", clientEvent.toByteArray());
            log.info("Sent client event to Kafka: {}", clientEvent);
        } catch (Exception e) {
            log.info("Failed to send client event to Kafka: {}", e.getMessage());
        }
    }
}
