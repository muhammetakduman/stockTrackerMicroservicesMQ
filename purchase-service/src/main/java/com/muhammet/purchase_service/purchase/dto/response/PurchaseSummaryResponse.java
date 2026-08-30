package com.muhammet.purchase_service.purchase.dto.response;

import java.math.BigDecimal;

public record PurchaseSummaryResponse(

        long todayPurchaseCount,

        BigDecimal todayPurchaseAmount,

        long pendingPurchases,

        long completedPurchases,

        long failedPurchases,

        long cancelledPurchases

) {
}