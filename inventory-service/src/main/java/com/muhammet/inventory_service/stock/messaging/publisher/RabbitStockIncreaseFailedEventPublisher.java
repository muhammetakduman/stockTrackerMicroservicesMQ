package com.muhammet.inventory_service.stock.messaging.publisher;

import com.muhammet.inventory_service.config.StockResultMessagingProperties;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitStockIncreaseFailedEventPublisher
        implements StockIncreaseFailedEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final StockResultMessagingProperties properties;

    @Override
    public void publish(
            StockIncreaseFailedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Stock increase failed event cannot be null"
        );

        rabbitTemplate.convertAndSend(
                properties.getExchange(),
                properties.getFailedRoutingKey(),
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

                    message.getMessageProperties()
                            .setHeader(
                                    "errorCode",
                                    event.errorCode()
                            );

                    return message;
                }
        );

        log.error(
                "StockIncreaseFailedEvent published. " +
                        "eventId={}, sourceEventId={}, " +
                        "purchaseId={}, stockItemId={}, " +
                        "errorCode={}, exchange={}, routingKey={}",
                event.eventId(),
                event.sourceEventId(),
                event.purchaseId(),
                event.stockItemId(),
                event.errorCode(),
                properties.getExchange(),
                properties.getFailedRoutingKey()
        );
    }
}