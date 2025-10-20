package com.sha.analytics_service.kafka;

import client.events.ClientEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = "client", groupId = "analytics-service")
    public void consumeEvent(byte[] eventBytes) {
        try {
            ClientEvent clientEvent = ClientEvent.parseFrom(eventBytes);
            // Process the client event (e.g., log it, store it in a database, etc.)
            log.info("Consumed client event: {}", clientEvent.toString());
        } catch (InvalidProtocolBufferException e) {
           log.info("Could not parse client event {}", e.getMessage());
        }

    }
}
