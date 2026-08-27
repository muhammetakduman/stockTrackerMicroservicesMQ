package com.muhammet.purchase_service.outbox.entity;


import com.muhammet.purchase_service.outbox.enums.OutboxStatus;
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
                        name = "uk_outbox_events_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_outbox_events_status_available_at",
                        columnList = "status, available_at"
                ),
                @Index(
                        name = "idx_outbox_events_aggregate",
                        columnList = "aggregate_type, aggregate_id"
                ),
                @Index(
                        name = "idx_outbox_events_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name= "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 50,
            updatable = false
    )
    private String aggregateType;

    @Column(
            name="aggregate_id",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String aggregateId;
    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false,
            updatable = false
    )
    private int eventVersion;

    @Column(
            name = "exchange_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String exchange;

    @Column(
            name = "routing_key",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String routingKey;

    /*
     * Event'in JSON hâli.
     */
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

    /*
     * Retry yapılacak en erken zaman.
     */
    @Column(
            name = "available_at",
            nullable = false
    )
    private Instant availableAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(
            name = "last_error",
            length = 1000
    )
    private String lastError;

    @Version
    private Long version;

    private OutboxEvent(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            int eventVersion,
            String exchange,
            String routingKey,
            String payload
    ) {
        this.eventId = Objects.requireNonNull(
                eventId,
                "Event ID cannot be null"
        );

        this.aggregateType = requireText(
                aggregateType,
                "Aggregate type"
        );

        this.aggregateId = requireText(
                aggregateId,
                "Aggregate ID"
        );

        this.eventType = requireText(
                eventType,
                "Event type"
        );

        if (eventVersion <= 0) {
            throw new IllegalArgumentException(
                    "Event version must be greater than zero"
            );
        }

        this.eventVersion = eventVersion;

        this.exchange = requireText(
                exchange,
                "Exchange"
        );

        this.routingKey = requireText(
                routingKey,
                "Routing key"
        );

        this.payload = requireText(
                payload,
                "Payload"
        );

        this.status = OutboxStatus.PENDING;
        this.attemptCount = 0;
        this.availableAt = Instant.now();
    }

    public static OutboxEvent pending(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            int eventVersion,
            String exchange,
            String routingKey,
            String payload
    ) {
        return new OutboxEvent(
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                eventVersion,
                exchange,
                routingKey,
                payload
        );
    }

    public void markPublished() {
        if (this.status == OutboxStatus.PUBLISHED) {
            return;
        }

        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }
    public void cancelBeforePublishing() {

        if (this.status == OutboxStatus.CANCELLED) {
            return;
        }

        if (this.status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending outbox event can be cancelled. " +
                            "Current status: " + this.status
            );
        }

        /*
         * Bir publish denemesi bile yapıldıysa güvenli şekilde
         * "mesaj kesin RabbitMQ'ya gitmedi" diyemeyiz.
         */
        if (this.attemptCount > 0) {
            throw new IllegalStateException(
                    "Outbox event cannot be cancelled after a publish attempt"
            );
        }

        if (this.publishedAt != null) {
            throw new IllegalStateException(
                    "Published outbox event cannot be cancelled"
            );
        }

        this.status = OutboxStatus.CANCELLED;
    }

    public void registerFailure(
            String errorMessage,
            Instant nextAttemptAt,
            int maximumAttempts
    ) {
        if (maximumAttempts <= 0) {
            throw new IllegalArgumentException(
                    "Maximum attempts must be greater than zero"
            );
        }

        this.attemptCount++;

        this.lastError = truncateError(errorMessage);

        if (this.attemptCount >= maximumAttempts) {
            this.status = OutboxStatus.FAILED;
            return;
        }

        this.status = OutboxStatus.PENDING;

        this.availableAt = Objects.requireNonNull(
                nextAttemptAt,
                "Next attempt time cannot be null"
        );
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();

        if (this.availableAt == null) {
            this.availableAt = this.createdAt;
        }
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return value.trim();
    }

    private static String truncateError(
            String errorMessage
    ) {
        if (errorMessage == null ||
                errorMessage.isBlank()) {
            return "Unknown outbox publishing error";
        }

        String normalized = errorMessage.trim();

        if (normalized.length() <= 1000) {
            return normalized;
        }

        return normalized.substring(0, 1000);
    }
}