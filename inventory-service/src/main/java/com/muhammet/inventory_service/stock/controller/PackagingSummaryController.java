package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.PackagingSummaryResponse;
import com.muhammet.inventory_service.stock.service.PackagingSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(
        name = "Inventory Summary",
        description = "Inventory operational summary endpoints"
)
public class PackagingSummaryController {

    private final PackagingSummaryService
            packagingSummaryService;


    @GetMapping("/packaging-summary")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'STOCK_MANAGER',
                'PRODUCTION_USER',
                'SALES_USER'
            )
            """)
    @Operation(
            summary = "Get packaging stock summary",
            description = """
                    Returns current bottle and packaging-set
                    stock levels used during perfume production.
                    """
    )
    public ResponseEntity<PackagingSummaryResponse>
    getPackagingSummary() {

        return ResponseEntity.ok(
                packagingSummaryService.getSummary()
        );
    }
}