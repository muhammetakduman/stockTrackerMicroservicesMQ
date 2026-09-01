package com.muhammet.sales_service.payment.repository;

import com.muhammet.sales_service.payment.domain.Payment;
import com.muhammet.sales_service.payment.domain.PaymentRecordStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllBySale_IdOrderByPaidAtAsc(Long saleId);

    @Query("""
            select coalesce(sum(p.amount), 0)
            from Payment p
            where p.sale.id = :saleId
              and p.status = :status
            """)
    BigDecimal sumAmountBySaleIdAndStatus(
            @Param("saleId") Long saleId,
            @Param("status") PaymentRecordStatus status
    );

    default BigDecimal sumRecordedAmountBySaleId(Long saleId) {

        return sumAmountBySaleIdAndStatus(
                saleId,
                PaymentRecordStatus.RECORDED
        );
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Payment p
            where p.id = :paymentId
            """)
    Optional<Payment> findByIdForUpdate(
            @Param("paymentId") UUID paymentId
    );
}