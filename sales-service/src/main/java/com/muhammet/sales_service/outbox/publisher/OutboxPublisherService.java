package com.muhammet.sales_service.outbox.publisher;

import com.muhammet.sales_service.outbox.entity.OutboxEvent;
import com.muhammet.sales_service.outbox.entity.OutboxStatus;
import com.muhammet.sales_service.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherService {

    private final OutboxEventRepository
            outboxEventRepository;

    private final RabbitTemplate
            rabbitTemplate;


    @Value("${app.outbox.publisher.batch-size:20}")
    private int batchSize;


    @Value("${app.outbox.publisher.max-attempts:5}")
    private int maxAttempts;


    @Value("${app.outbox.publisher.confirm-timeout-ms:5000}")
    private long confirmTimeoutMs;


    @Transactional
    public void publishPendingBatch() {

        List<OutboxEvent> events =
                outboxEventRepository.findBatchForPublishing(
                        OutboxStatus.PENDING,
                        PageRequest.of(
                                0,
                                batchSize
                        )
                );


        if (events.isEmpty()) {
            return;
        }


        log.debug(
                "Outbox publish batch started. eventCount={}",
                events.size()
        );


        for (OutboxEvent event : events) {

            publishOne(
                    event
            );
        }
    }


    private void publishOne(
            OutboxEvent event
    ) {

        try {

            Message message =
                    createRabbitMessage(
                            event
                    );


            CorrelationData correlationData =
                    new CorrelationData(
                            event.getEventId().toString()
                    );


            rabbitTemplate.send(
                    event.getExchangeName(),
                    event.getRoutingKey(),
                    message,
                    correlationData
            );


            CorrelationData.Confirm confirm =
                    correlationData
                            .getFuture()
                            .get(
                                    confirmTimeoutMs,
                                    TimeUnit.MILLISECONDS
                            );


            /*
             * Broker mesajı kabul etti ancak
             * hiçbir queue'ya route edilemedi mi?
             */
            if (correlationData.getReturned()
                    != null) {

                String error =
                        "RabbitMQ returned message. " +
                                "exchange=" +
                                event.getExchangeName() +
                                ", routingKey=" +
                                event.getRoutingKey() +
                                ", replyText=" +
                                correlationData
                                        .getReturned()
                                        .getReplyText();


                registerFailure(
                        event,
                        error
                );

                return;
            }


            /*
             * Broker ACK vermedi.
             */
            if (!confirm.ack()) {

                String error =
                        "RabbitMQ publisher NACK. reason=" +
                                confirm.reason();


                registerFailure(
                        event,
                        error
                );

                return;
            }


            /*
             * ACK + route başarılı.
             */
            event.markPublished();


            log.info(
                    "Outbox event published. " +
                            "eventId={}, eventType={}, aggregateId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getAggregateId()
            );

        } catch (Exception exception) {

            registerFailure(
                    event,
                    buildErrorMessage(
                            exception
                    )
            );
        }
    }


    private Message createRabbitMessage(
            OutboxEvent event
    ) {

        byte[] body =
                event.getPayload()
                        .getBytes(
                                StandardCharsets.UTF_8
                        );


        return MessageBuilder
                .withBody(body)
                .setContentType(
                        "application/json"
                )
                .setMessageId(
                        event.getEventId()
                                .toString()
                )
                .build();
    }


    private void registerFailure(
            OutboxEvent event,
            String error
    ) {

        event.registerPublishFailure(
                error
        );


        if (event.getAttemptCount()
                >= maxAttempts) {

            event.markFailed(
                    error
            );


            log.error(
                    "Outbox event permanently failed. " +
                            "eventId={}, eventType={}, attempts={}, error={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getAttemptCount(),
                    error
            );

            return;
        }


        log.warn(
                "Outbox publish failed; event will be retried. " +
                        "eventId={}, eventType={}, attempt={}/{}, error={}",
                event.getEventId(),
                event.getEventType(),
                event.getAttemptCount(),
                maxAttempts,
                error
        );
    }


    private String buildErrorMessage(
            Exception exception
    ) {

        String message =
                exception.getMessage();

        if (message == null ||
                message.isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception
                .getClass()
                .getSimpleName()
                +
                ": "
                +
                message;
    }
}