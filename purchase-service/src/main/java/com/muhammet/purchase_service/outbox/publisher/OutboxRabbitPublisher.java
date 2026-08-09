package com.muhammet.purchase_service.outbox.publisher;

import com.muhammet.purchase_service.outbox.config.OutboxPublisherProperties;
import com.muhammet.purchase_service.outbox.entity.OutboxEvent;
import com.muhammet.purchase_service.outbox.exception.OutboxPublishException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class OutboxRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OutboxPublisherProperties properties;

    public void publish(OutboxEvent outboxEvent) {

        Message message = MessageBuilder
                .withBody(
                        outboxEvent.getPayload()
                                .getBytes(StandardCharsets.UTF_8)
                )
                .setContentType(
                        MessageProperties.CONTENT_TYPE_JSON
                )
                .setContentEncoding(
                        StandardCharsets.UTF_8.name()
                )
                .setMessageId(
                        outboxEvent.getEventId().toString()
                )
                .setType(
                        outboxEvent.getEventType()
                )
                .setDeliveryMode(
                        MessageDeliveryMode.PERSISTENT
                )
                .setHeader(
                        "eventVersion",
                        outboxEvent.getEventVersion()
                )
                .setHeader(
                        "aggregateType",
                        outboxEvent.getAggregateType()
                )
                .setHeader(
                        "aggregateId",
                        outboxEvent.getAggregateId()
                )
                .build();

        CorrelationData correlationData =
                new CorrelationData(
                        outboxEvent.getEventId().toString()
                );

        rabbitTemplate.send(
                outboxEvent.getExchange(),
                outboxEvent.getRoutingKey(),
                message,
                correlationData
        );

        waitForConfirmation(
                outboxEvent,
                correlationData
        );
    }

    private void waitForConfirmation(
            OutboxEvent outboxEvent,
            CorrelationData correlationData
    ) {
        try {
            CorrelationData.Confirm confirm =
                    correlationData
                            .getFuture()
                            .get(
                                    properties
                                            .getConfirmTimeoutMs(),
                                    TimeUnit.MILLISECONDS
                            );

            /*
             * Exchange mesajı kabul etmediyse veya broker
             * NACK döndüyse PUBLISHED yapmıyoruz.
             */
            if (!confirm.ack()) {
                throw new OutboxPublishException(
                        "RabbitMQ returned NACK. eventId="
                                + outboxEvent.getEventId()
                                + ", reason="
                                + confirm.reason()
                );
            }

            /*
             * Broker ACK vermiş olsa bile routing key hiçbir
             * queue ile eşleşmemiş olabilir.
             */
            if (correlationData.getReturned() != null) {
                throw new OutboxPublishException(
                        "RabbitMQ returned unroutable message. "
                                + "eventId="
                                + outboxEvent.getEventId()
                                + ", returned="
                                + correlationData.getReturned()
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new OutboxPublishException(
                    "Interrupted while waiting for RabbitMQ confirm. "
                            + "eventId="
                            + outboxEvent.getEventId(),
                    exception
            );

        } catch (ExecutionException |
                 TimeoutException exception) {

            throw new OutboxPublishException(
                    "RabbitMQ confirmation could not be received. "
                            + "eventId="
                            + outboxEvent.getEventId(),
                    exception
            );
        }
    }
}