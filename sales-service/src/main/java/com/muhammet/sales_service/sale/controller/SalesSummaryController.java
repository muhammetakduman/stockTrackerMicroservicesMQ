package com.muhammet.sales_service.sale.controller;

import com.muhammet.sales_service.sale.dto.response.SalesSummaryResponse;
import com.muhammet.sales_service.sale.service.SalesSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(
        name = "Sales Summary",
        description = "Sales dashboard summary operations"
)
public class SalesSummaryController {

    private final SalesSummaryService salesSummaryService;


    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get sales summary",
            description = """
                    Returns sales dashboard metrics.

                    Today's revenue includes only COMPLETED sales.

                    FAILED and PENDING_STOCK_UPDATE sales are not
                    included in today's revenue.
                    """
    )
    public ResponseEntity<SalesSummaryResponse> getSummary() {

        return ResponseEntity.ok(
                salesSummaryService.getSummary()
        );
    }
}