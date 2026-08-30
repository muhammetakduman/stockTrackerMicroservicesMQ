package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.InventorySummaryResponse;
import com.muhammet.inventory_service.stock.service.InventorySummaryService;

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
        description = "Inventory dashboard summary operations"
)
public class InventorySummaryController {

    private final InventorySummaryService inventorySummaryService;


    @GetMapping("/summary")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'STOCK_MANAGER')"
    )
    @Operation(
            summary = "Get inventory summary",
            description = """
                    Returns inventory-level dashboard metrics.

                    Includes:
                    - total stock items
                    - active stock items
                    - inactive stock items
                    - out-of-stock items
                    - today's stock movement count
                    """
    )
    public ResponseEntity<InventorySummaryResponse> getSummary() {

        return ResponseEntity.ok(
                inventorySummaryService.getSummary()
        );
    }
}