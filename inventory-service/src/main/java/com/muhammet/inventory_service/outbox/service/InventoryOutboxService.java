package com.muhammet.inventory_service.outbox.service;

import com.muhammet.inventory_service.config.StockResultMessagingProperties;
import com.muhammet.inventory_service.outbox.entity.OutboxEvent;
import com.muhammet.inventory_service.outbox.exception.OutboxSerializationException;
import com.muhammet.inventory_service.outbox.repository.OutboxEventRepository;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseCompletedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryOutboxService {
    private static final String AGGREGATE_TYPE = "STOCK_ITEM";
    private  final OutboxEventRepository outboxEventRepository;
    private final StockResultMessagingProperties messagingProperties;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent appendCompletedEvent(
            StockIncreaseCompletedEvent event
    ) {
        if(event == null){
            throw new IllegalArgumentException("Stock increase completed event cannot be null");
        }
        ensureEventDoesNotExist(event.eventId());
        String payload = serializeCompletedEvent(event);

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),
                        messagingProperties.getRoutingKey(),
                        payload
                );
        return outboxEventRepository.save(outboxEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboxEvent appendFailedEvent(
            StockIncreaseFailedEvent event
    ) {
        if(event == null){
            throw new IllegalArgumentException("Stock increase failed event cannot be null");
        }
        ensureEventDoesNotExist(event.eventId());
        String payload = serializeFailedEvent(event);

        OutboxEvent outboxEvent  =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),
                        messagingProperties.getRoutingKey(),
                        payload
                );
        return outboxEventRepository.save(outboxEvent);
    }

    private String serializeFailedEvent(StockIncreaseFailedEvent event) {
        {
            try {
                return jsonMapper.writeValueAsString(event);

            }catch (JacksonException exception){
                throw new OutboxSerializationException(
                        event.eventId(),
                        exception
                );
            }
        }
    }


    private String serializeCompletedEvent(StockIncreaseCompletedEvent event) {
        try {
            return jsonMapper.writeValueAsString(event);

        }catch (JacksonException exception){
            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }

    private void ensureEventDoesNotExist(UUID eventId) {
        if(outboxEventRepository.existsByEventId(eventId)){
            throw new IllegalArgumentException(
                    "Outbox event already exists. eventId=" + eventId
            );
        }
    }
}
