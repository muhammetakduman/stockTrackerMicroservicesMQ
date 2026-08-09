package com.muhammet.purchase_service.inbox.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_event_consumer",
                        columnNames = {
                                "consumer_name",
                                "event_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_processed_events_processed_at",
                        columnList = "processed_at"

                )

        }
)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {
    @Id
    private UUID id;
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name ="event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;
    @Column(
            name = "consumer_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String consumerName;

    @Column(
            name = "processed_at",
            nullable = false,
            updatable = false
    )
    private Instant processedAt;

}
