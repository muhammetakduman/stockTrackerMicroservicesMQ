package com.muhammet.sales_service.payment.dto.response;

import com.muhammet.sales_service.payment.domain.SalePaymentStatus;

import java.math.BigDecimal;

public record SalePaymentSummaryResponse(

        Long saleId,

        BigDecimal totalPrice,

        BigDecimal totalPaid,

        BigDecimal outstandingAmount,

        SalePaymentStatus paymentStatus

) {
}