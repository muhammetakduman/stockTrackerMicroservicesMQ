package com.muhammet.sales_service.sale.dto.response;

import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.domain.SaleStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleResponse(

        Long id,

        UUID sellerId,

        UUID customerId,

        String customerName,

        UUID stockItemId,

        BigDecimal quantity,

        BigDecimal unitPrice,

        BigDecimal totalPrice,

        String note,

        SaleStatus status,

        String failureReason,

        Instant soldAt,

        Instant createdAt,

        Instant updatedAt

) {

    public static SaleResponse from(
            Sale sale
    ) {

        return new SaleResponse(
                sale.getId(),
                sale.getSellerId(),
                sale.getCustomerId(),
                sale.getCustomerNameSnapshot(),
                sale.getStockItemId(),
                sale.getQuantity(),
                sale.getUnitPrice(),
                sale.getTotalPrice(),
                sale.getNote(),
                sale.getStatus(),
                sale.getFailureReason(),
                sale.getSoldAt(),
                sale.getCreatedAt(),
                sale.getUpdatedAt()
        );
    }
}