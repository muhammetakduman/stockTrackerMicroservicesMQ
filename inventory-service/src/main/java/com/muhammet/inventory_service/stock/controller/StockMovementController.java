package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.PageResponse;
import com.muhammet.inventory_service.stock.dto.StockMovementResponse;

import com.muhammet.inventory_service.stock.enums.StockMovementType;

import com.muhammet.inventory_service.stock.service.StockMovementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
@Tag(
        name = "Stock Movements",
        description = "Stock movement history and query operations"
)
public class StockMovementController {

    private final StockMovementService stockMovementService;


    @GetMapping
    @Operation(
            summary = "List stock movements",
            description = """
                    Returns paginated stock movement history.

                    Optional filters:
                    - movement type
                    - stock item
                    - occurrence date range

                    Results are ordered by newest movement first.
                    """
    )
    public ResponseEntity<PageResponse<StockMovementResponse>> findAll(

            @Parameter(
                    description = "Filter by movement type"
            )
            @RequestParam(required = false)
            StockMovementType type,


            @Parameter(
                    description = "Filter by stock item ID"
            )
            @RequestParam(required = false)
            UUID stockItemId,


            @Parameter(
                    description = "Beginning of occurrence date range",
                    example = "2026-08-01T00:00:00Z"
            )
            @RequestParam(required = false)
            Instant from,


            @Parameter(
                    description = "End of occurrence date range",
                    example = "2026-08-31T23:59:59Z"
            )
            @RequestParam(required = false)
            Instant to,


            @Parameter(
                    description = "Zero-based page number"
            )
            @RequestParam(defaultValue = "0")
            int page,


            @Parameter(
                    description = "Number of records per page"
            )
            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                stockMovementService.findAll(
                        type,
                        stockItemId,
                        from,
                        to,
                        page,
                        size
                )
        );
    }
}