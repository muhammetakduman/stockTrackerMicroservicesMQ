package com.muhammet.inventory_service.stock.repository;

import com.muhammet.inventory_service.stock.entity.StockBalance;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
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
     * Tüm stock balance kayıtlarını,
     * ilişkili StockItem bilgileri ile beraber getirir.
     */
    @EntityGraph(attributePaths = "stockItem")
    @Query("""
            SELECT sb
            FROM StockBalance sb
            ORDER BY sb.stockItem.name ASC
            """)
    List<StockBalance> findAllWithStockItem();


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

    @EntityGraph(attributePaths = "stockItem")
    @Query("""
        SELECT sb
        FROM StockBalance sb
        WHERE sb.stockItem.active = true
          AND (sb.onHandQuantity - sb.reservedQuantity) <= :threshold
        ORDER BY (sb.onHandQuantity - sb.reservedQuantity) ASC
        """)
    List<StockBalance> findLowStockBalances(
            @Param("threshold")
            BigDecimal threshold
    );

    @Query("""
         SELECT COUNT(sb)
         FROM StockBalance sb
         WHERE sb.stockItem.active = true
           AND (
               sb.onHandQuantity - sb.reservedQuantity
           ) <= 0
         """)
    long countOutOfStock();

    /**
     * Production işleminde 4 StockBalance'ı deterministik sırayla (UUID order)
     * PESSIMISTIC_WRITE lock altında getirir.
     *
     * Deadlock prevention: Tüm concurrent transaction'lar aynı sırayla lock alır.
     * Caller: stockItemIds listesini UUID.compareTo() sırasına göre sıralı geçmeli.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "stockItem")
    @Query("""
            SELECT sb
            FROM StockBalance sb
            WHERE sb.stockItem.id IN :stockItemIds
            ORDER BY sb.stockItem.id ASC
            """)
    List<StockBalance> findAllByStockItemIdsForUpdate(
            @Param("stockItemIds")
            List<UUID> stockItemIds
    );
}