package com.muhammet.sales_service.outbox.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_outbox_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_outbox_status_created_at",
                        columnList = "status, created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "aggregate_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String aggregateId;

    @Column(
            name = "exchange_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String exchangeName;

    @Column(
            name = "routing_key",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String routingKey;

    @Column(
            name = "payload",
            nullable = false,
            updatable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private OutboxStatus status;

    @Column(
            name = "attempt_count",
            nullable = false
    )
    private int attemptCount;

    @Column(
            name = "last_error",
            length = 1000
    )
    private String lastError;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "published_at"
    )
    private Instant publishedAt;


    private OutboxEvent(
            UUID eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String exchangeName,
            String routingKey,
            String payload
    ) {

        this.id = UUID.randomUUID();

        this.eventId =
                Objects.requireNonNull(
                        eventId,
                        "Event ID cannot be null"
                );

        this.eventType =
                requireText(
                        eventType,
                        "Event type"
                );

        this.aggregateType =
                requireText(
                        aggregateType,
                        "Aggregate type"
                );

        this.aggregateId =
                requireText(
                        aggregateId,
                        "Aggregate ID"
                );

        this.exchangeName =
                requireText(
                        exchangeName,
                        "Exchange name"
                );

        this.routingKey =
                requireText(
                        routingKey,
                        "Routing key"
                );

        this.payload =
                requireText(
                        payload,
                        "Payload"
                );

        this.status =
                OutboxStatus.PENDING;

        this.attemptCount = 0;

        this.createdAt =
                Instant.now();
    }


    public static OutboxEvent pending(
            UUID eventId,
            String eventType,
            String aggregateType,
            String aggregateId,
            String exchangeName,
            String routingKey,
            String payload
    ) {

        return new OutboxEvent(
                eventId,
                eventType,
                aggregateType,
                aggregateId,
                exchangeName,
                routingKey,
                payload
        );
    }


    public void markPublished() {

        this.status =
                OutboxStatus.PUBLISHED;

        this.publishedAt =
                Instant.now();

        this.lastError = null;
    }


    public void registerPublishFailure(
            String error
    ) {

        this.attemptCount++;

        this.lastError =
                error == null
                        ? "Unknown publish error"
                        : error;
    }


    public void markFailed(
            String error
    ) {

        this.status =
                OutboxStatus.FAILED;

        this.lastError =
                error == null
                        ? "Unknown publish error"
                        : error;
    }


    private static String requireText(
            String value,
            String fieldName
    ) {

        if (value == null ||
                value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return value.trim();
    }
}