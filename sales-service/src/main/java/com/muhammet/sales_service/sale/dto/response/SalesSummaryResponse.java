package com.muhammet.sales_service.sale.dto.response;

import java.math.BigDecimal;

public record SalesSummaryResponse(

        long todaySalesCount,

        BigDecimal todayRevenue,

        long pendingSales,

        long completedSales,

        long failedSales

) {
}