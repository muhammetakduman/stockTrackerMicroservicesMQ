package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    boolean existsBySourceEventId(UUID sourceEventId);

    Optional<StockMovement> findBySourceEventId(
UUID sourceEventId
    );
    List<StockMovement>
    findAllByStockItem_IdOrderByCreatedAtDesc(
            UUID stockItemId
    );
}
