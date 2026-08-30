package com.muhammet.sales_service.sale.repository;

import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.domain.SaleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

public interface SaleRepository
        extends JpaRepository<Sale, Long>, JpaSpecificationExecutor<Sale> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s
            FROM Sale s
            WHERE s.id = :saleId
            """)
    Optional<Sale> findByIdForUpdate(
            @Param("saleId")
            Long saleId
    );
    long countByStatus(
            SaleStatus status
    );

    long countBySoldAtGreaterThanEqualAndSoldAtLessThan(
            Instant from,
            Instant to
    );

    @Query("""
        SELECT COALESCE(
            SUM(s.totalPrice),
            0
        )
        FROM Sale s
        WHERE s.soldAt >= :from
          AND s.soldAt < :to
          AND s.status = :status
        """)
    BigDecimal sumRevenue(
            @Param("from")
            Instant from,

            @Param("to")
            Instant to,

            @Param("status")
            SaleStatus status
    );
}