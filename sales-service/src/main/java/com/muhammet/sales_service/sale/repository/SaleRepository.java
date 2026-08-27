package com.muhammet.sales_service.sale.repository;

import com.muhammet.sales_service.sale.domain.Sale;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}