package com.muhammet.purchase_service.purchase.messaging.publisher;

import com.muhammet.purchase_service.config.MessagingProperties;
import com.muhammet.purchase_service.purchase.messaging.event.PurchaseCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitPurchaseEventPublisher
        implements PurchaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties messagingProperties;

    @Override
    public void publish(PurchaseCreatedEvent event) {

        rabbitTemplate.convertAndSend(
                messagingProperties.getExchange(),
                messagingProperties.getRoutingKey(),
                event,
                message -> {
                    message.getMessageProperties()
                            .setMessageId(event.eventId().toString());

                    message.getMessageProperties()
                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);

                    message.getMessageProperties()
                            .setHeader(
                                    "eventType",
                                    event.eventType()
                            );

                    message.getMessageProperties()
                            .setHeader(
                                    "eventVersion",
                                    event.eventVersion()
                            );

                    message.getMessageProperties()
                            .setHeader(
                                    "purchaseId",
                                    event.purchaseId()
                            );

                    return message;
                }
        );

        log.info(
                "Purchase event publish request sent. " +
                        "eventId={}, purchaseId={}, exchange={}, routingKey={}",
                event.eventId(),
                event.purchaseId(),
                messagingProperties.getExchange(),
                messagingProperties.getRoutingKey()
        );
    }
}