package com.muhammet.sales_service.payment.repository;

import com.muhammet.sales_service.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllBySale_IdOrderByPaidAtAsc(Long saleId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.sale.id = :saleId
              and p.status = com.muhammet.sales_service.payment.domain.PaymentRecordStatus.RECORDED
            """)
    BigDecimal sumRecordedAmountBySaleId(
            @Param("saleId") Long saleId
    );
}