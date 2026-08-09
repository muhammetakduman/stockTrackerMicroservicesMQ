package com.muhammet.purchase_service.inbox.repository;

import com.muhammet.purchase_service.inbox.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    @Modifying
    @Query(
            value = """
                    INSERT INTO processed_events(
                    id,
                    event_id,
                    event_type,
                    consumer_name,
                    processed_at
                    )
                    VALUES (
                    :id,
                    :eventId,
                    :eventType,
                    :consumerName,
                    :processedAt
                    )
                    ON CONFLICT(
                    event_id,
                    consumer_name
                    )
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType,
            @Param("consumerName") String consumerName,
            @Param("processedAt") java.time.Instant processedAt
    );
}
