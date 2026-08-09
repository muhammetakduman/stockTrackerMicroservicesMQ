package com.muhammet.sales_service.outbox.repository;

import com.muhammet.sales_service.outbox.entity.OutboxEvent;
import com.muhammet.sales_service.outbox.entity.OutboxStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM OutboxEvent o
            WHERE o.status = :status
            ORDER BY o.createdAt ASC
            """)
    List<OutboxEvent> findBatchForPublishing(
            @Param("status") OutboxStatus status,
            Pageable pageable
    );
}