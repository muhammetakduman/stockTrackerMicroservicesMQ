package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockBalance;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StockBalanceRepository
        extends JpaRepository<StockBalance, UUID> {


    /*
     * Sadece okuma gereken yerlerde kullanılabilir.
     */
    @EntityGraph(attributePaths = "stockItem")
    Optional<StockBalance> findByStockItemId(
            UUID stockItemId
    );


    /*
     * Stock mutation işlemleri için.
     *
     * Transaction bitene kadar ilgili
     * StockBalance satırını WRITE lock altında tutar.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "stockItem")
    @Query("""
            SELECT sb
            FROM StockBalance sb
            WHERE sb.stockItem.id = :stockItemId
            """)
    Optional<StockBalance> findByStockItemIdForUpdate(
            @Param("stockItemId")
            UUID stockItemId
    );
}