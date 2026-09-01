package com.muhammet.sales_service.payment.dto.response;

public record PaymentOperationResponse(

        PaymentResponse payment,

        SalePaymentSummaryResponse summary

) {
}