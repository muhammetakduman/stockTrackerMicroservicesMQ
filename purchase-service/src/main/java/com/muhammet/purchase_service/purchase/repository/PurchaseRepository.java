package com.muhammet.purchase_service.purchase.repository;

import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase , Long> , JpaSpecificationExecutor<Purchase> {

    List<Purchase> findAllByStatusOrderByCreatedAtDesc(PurchaseStatus status);

    List<Purchase> findAllByStockItemIdOrderByCreatedAtDesc(UUID stockItemId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Purchase p
        WHERE p.id = :id
        """)
    Optional<Purchase> findByIdForUpdate(
            @Param("id") Long id
    );

    long countByStatus(
            PurchaseStatus status
    );

    long countByPurchasedAtGreaterThanEqualAndPurchasedAtLessThan(
            Instant from,
            Instant to
    );
    @Query("""
        SELECT COALESCE(
            SUM(p.quantity * p.unitPrice),
            0
        )
        FROM Purchase p
        WHERE p.purchasedAt >= :from
          AND p.purchasedAt < :to
          AND p.status = :status
        """)
    BigDecimal sumPurchaseAmount(
            @Param("from")
            Instant from,

            @Param("to")
            Instant to,

            @Param("status")
            PurchaseStatus status
    );
}
