package com.muhammet.purchase_service.purchase.controller;

import com.muhammet.purchase_service.purchase.dto.response.PurchaseSummaryResponse;
import com.muhammet.purchase_service.purchase.service.PurchaseSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(
        name = "Purchase Summary",
        description = "Purchase dashboard summary operations"
)
public class PurchaseSummaryController {

    private final PurchaseSummaryService purchaseSummaryService;


    @GetMapping("/summary")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'STOCK_MANAGER')"
    )
    @Operation(
            summary = "Get purchase summary",
            description = """
                    Returns purchase dashboard metrics.

                    Today's purchase amount includes only
                    COMPLETED purchases.

                    FAILED and CANCELLED purchases are not
                    included in today's purchase amount.
                    """
    )
    public ResponseEntity<PurchaseSummaryResponse> getSummary() {

        return ResponseEntity.ok(
                purchaseSummaryService.getSummary()
        );
    }
}