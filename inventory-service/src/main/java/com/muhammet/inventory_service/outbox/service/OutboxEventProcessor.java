package com.muhammet.inventory_service.outbox.service;

import com.muhammet.inventory_service.outbox.config.OutboxPublisherProperties;
import com.muhammet.inventory_service.outbox.entity.OutboxEvent;
import com.muhammet.inventory_service.outbox.enums.OutboxStatus;
import com.muhammet.inventory_service.outbox.publisher.OutboxRabbitPublisher;
import com.muhammet.inventory_service.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {

    private static final long MAX_BACKOFF_SECONDS = 60;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRabbitPublisher outboxRabbitPublisher;
    private final OutboxPublisherProperties properties;

    @Transactional
    public void process(
            UUID outboxEventId
    ) {
        OutboxEvent outboxEvent =
                outboxEventRepository
                        .findByIdForUpdate(outboxEventId)
                        .orElse(null);

        if (outboxEvent == null) {
            return;
        }

        /*
         * Başka worker daha önce işlemiş olabilir.
         */
        if (outboxEvent.getStatus() !=
                OutboxStatus.PENDING) {
            return;
        }

        if (outboxEvent.getAvailableAt()
                .isAfter(Instant.now())) {
            return;
        }

        try {
            outboxRabbitPublisher.publish(outboxEvent);

            outboxEvent.markPublished();

            log.info(
                    "Inventory outbox event published. " +
                            "outboxId={}, eventId={}, " +
                            "eventType={}, aggregateId={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getAggregateId()
            );

        } catch (RuntimeException exception) {
            long backoffSeconds =
                    calculateBackoffSeconds(
                            outboxEvent.getAttemptCount()
                    );

            Instant nextAttemptAt =
                    Instant.now()
                            .plusSeconds(backoffSeconds);

            outboxEvent.registerFailure(
                    resolveErrorMessage(exception),
                    nextAttemptAt,
                    properties.getMaxAttempts()
            );

            log.error(
                    "Inventory outbox publish failed. " +
                            "outboxId={}, eventId={}, " +
                            "attemptCount={}, status={}, " +
                            "nextAttemptAt={}",
                    outboxEvent.getId(),
                    outboxEvent.getEventId(),
                    outboxEvent.getAttemptCount(),
                    outboxEvent.getStatus(),
                    outboxEvent.getStatus()
                            == OutboxStatus.PENDING
                            ? outboxEvent.getAvailableAt()
                            : null,
                    exception
            );
        }
    }

    private long calculateBackoffSeconds(
            int currentAttemptCount
    ) {
        double calculated =
                Math.pow(
                        2,
                        currentAttemptCount + 1
                );

        return Math.min(
                (long) calculated,
                MAX_BACKOFF_SECONDS
        );
    }

    private String resolveErrorMessage(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current.getCause() != null &&
                current.getCause() != current) {
            current = current.getCause();
        }

        String message = current.getMessage();

        if (message == null || message.isBlank()) {
            return current
                    .getClass()
                    .getSimpleName();
        }

        return message;
    }
}