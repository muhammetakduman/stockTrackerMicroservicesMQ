package com.muhammet.inventory_service.outbox.repository;

import com.muhammet.inventory_service.outbox.entity.OutboxEvent;
import com.muhammet.inventory_service.outbox.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    boolean existsByEventId(UUID eventId);

    List<OutboxEvent>
    findAllByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status,
            Instant availableAt,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select oe
            from OutboxEvent oe
            where oe.id = :id
            """)
    Optional<OutboxEvent> findByIdForUpdate(
            @Param("id") UUID id
    );
}