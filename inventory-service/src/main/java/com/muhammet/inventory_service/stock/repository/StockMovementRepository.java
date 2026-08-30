package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockMovement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID>,
        JpaSpecificationExecutor<StockMovement> {

    boolean existsBySourceEventId(UUID sourceEventId);

    Optional<StockMovement> findBySourceEventId(
            UUID sourceEventId
    );

    List<StockMovement>
    findAllByStockItem_IdOrderByCreatedAtDesc(
            UUID stockItemId
    );
    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Instant from,
            Instant to
    );
}