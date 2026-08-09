package com.muhammet.inventory_service.stock.messaging.publisher;

import com.muhammet.inventory_service.config.StockResultMessagingProperties;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitStockIncreaseCompletedEventPublisher
        implements StockIncreaseCompletedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final StockResultMessagingProperties properties;

    @Override
    public void publish(StockIncreaseCompletedEvent event) {

        Objects.requireNonNull(
                event,
                "Stock increase completed event cannot be null"
        );

        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getRoutingKey(),
                event,
                message -> {

                    message.getMessageProperties()
                            .setMessageId(
                                    event.eventId().toString()
                            );

                    message.getMessageProperties()
                            .setType(event.eventType());

                    message.getMessageProperties()
                            .setDeliveryMode(
                                    MessageDeliveryMode.PERSISTENT
                            );

                    message.getMessageProperties()
                            .setHeader(
                                    "eventVersion",
                                    event.eventVersion()
                            );

                    message.getMessageProperties()
                            .setHeader(
                                    "sourceEventId",
                                    event.sourceEventId().toString()
                            );

                    return message;
                }
        );

        log.info(
                "StockIncreaseCompletedEvent published. " +
                        "eventId={}, sourceEventId={}, purchaseId={}, " +
                        "stockItemId={}, exchange={}, routingKey={}",
                event.eventId(),
                event.sourceEventId(),
                event.purchaseId(),
                event.stockItemId(),
                properties.getExchange(),
                properties.getRoutingKey()
        );
    }
}