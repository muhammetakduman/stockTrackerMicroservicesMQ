package com.muhammet.purchase_service.outbox.service;

import com.muhammet.purchase_service.config.MessagingProperties;
import com.muhammet.purchase_service.outbox.entity.OutboxEvent;
import com.muhammet.purchase_service.outbox.exception.OutboxSerializationException;
import com.muhammet.purchase_service.outbox.repository.OutboxEventRepository;
import com.muhammet.purchase_service.purchase.messaging.event.PurchaseCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class PurchaseOutboxService {

    private static final String AGGREGATE_TYPE =
            "PURCHASE";

    private final OutboxEventRepository outboxEventRepository;
    private final MessagingProperties messagingProperties;
    private final JsonMapper jsonMapper;

    public OutboxEvent append(
            PurchaseCreatedEvent event
    ) {
        if (outboxEventRepository.existsByEventId(
                event.eventId()
        )) {
            throw new IllegalStateException(
                    "Outbox event already exists. eventId="
                            + event.eventId()
            );
        }

        String payload = serialize(event);

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.purchaseId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),
                        messagingProperties.getRoutingKey(),
                        payload
                );

        return outboxEventRepository.save(outboxEvent);
    }

    private String serialize(
            PurchaseCreatedEvent event
    ) {
        try {
            return jsonMapper.writeValueAsString(event);

        } catch (JacksonException exception) {
            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }
}