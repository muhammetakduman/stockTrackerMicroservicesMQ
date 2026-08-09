package com.muhammet.purchase_service.purchase.dto.response;

import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PurchaseResponse(

        Long id,

        UUID stockItemId,

        BigDecimal quantity,

        BigDecimal unitPrice,

        BigDecimal totalAmount,

        String supplierName,

        PurchaseStatus status,

        Instant purchasedAt,

        String failureReason,

        Instant createdAt,

        Instant updatedAt

) {

    public static PurchaseResponse from(Purchase purchase) {
        Objects.requireNonNull(
                purchase,
                "Purchase cannot be null"
        );

        BigDecimal totalAmount = purchase.getQuantity()
                .multiply(purchase.getUnitPrice());

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getStockItemId(),
                purchase.getQuantity(),
                purchase.getUnitPrice(),
                totalAmount,
                purchase.getSupplierName(),
                purchase.getStatus(),
                purchase.getPurchasedAt(),
                purchase.getFailureReason(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }
}