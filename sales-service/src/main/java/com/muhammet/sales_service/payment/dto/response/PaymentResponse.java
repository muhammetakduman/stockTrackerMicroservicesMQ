package com.muhammet.sales_service.payment.dto.response;

import com.muhammet.sales_service.payment.domain.Payment;
import com.muhammet.sales_service.payment.domain.PaymentRecordStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID id,

        Long saleId,

        BigDecimal amount,

        Instant paidAt,

        String note,

        UUID recordedByUserId,

        PaymentRecordStatus status,

        String voidReason,

        UUID voidedByUserId,

        Instant voidedAt,

        Instant createdAt

) {

    public static PaymentResponse from(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getSale().getId(),
                payment.getAmount(),
                payment.getPaidAt(),
                payment.getNote(),
                payment.getRecordedByUserId(),
                payment.getStatus(),
                payment.getVoidReason(),
                payment.getVoidedByUserId(),
                payment.getVoidedAt(),
                payment.getCreatedAt()
        );
    }
}