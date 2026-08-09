package com.muhammet.purchase_service.outbox.service;

import com.muhammet.purchase_service.outbox.config.OutboxPublisherProperties;
import com.muhammet.purchase_service.outbox.entity.OutboxEvent;
import com.muhammet.purchase_service.outbox.enums.OutboxStatus;
import com.muhammet.purchase_service.outbox.publisher.OutboxRabbitPublisher;
import com.muhammet.purchase_service.outbox.repository.OutboxEventRepository;
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
    public void process(UUID outboxEventId) {

        OutboxEvent outboxEvent =
                outboxEventRepository
                        .findByIdForUpdate(outboxEventId)
                        .orElse(null);

        if (outboxEvent == null) {
            return;
        }

        /*
         * Başka bir worker bu kaydı daha önce
         * yayınlamış olabilir.
         */
        if (outboxEvent.getStatus() !=
                OutboxStatus.PENDING) {
            return;
        }

        Instant now = Instant.now();

        if (outboxEvent.getAvailableAt()
                .isAfter(now)) {
            return;
        }

        try {
            outboxRabbitPublisher.publish(outboxEvent);

            outboxEvent.markPublished();

            log.info(
                    "Outbox event published. " +
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
                    "Outbox publish failed. " +
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
        /*
         * 1. hata → 2 saniye
         * 2. hata → 4 saniye
         * 3. hata → 8 saniye
         * 4. hata → 16 saniye
         * Üst sınır → 60 saniye
         */
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