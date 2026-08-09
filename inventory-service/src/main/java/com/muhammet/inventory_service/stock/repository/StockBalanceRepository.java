package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockBalance;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, UUID> {

    @EntityGraph(attributePaths = "stockItem")
    Optional<StockBalance> findByStockItemId(
            UUID stockItemId
    );
}