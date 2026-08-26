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
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/stock-items/{stockItemId}/movements"
)
@RequiredArgsConstructor
@Tag(
        name = "Stock Item Movements",
        description = "Stock movement history for individual stock items"
)
public class StockItemMovementController {

    private final StockMovementService stockMovementService;


    @GetMapping
    @Operation(
            summary = "Get stock item movement history",
            description = """
                    Returns paginated movement history for a specific
                    stock item.

                    The history can optionally be filtered by movement
                    type and occurrence date range.
                    """
    )
    public ResponseEntity<PageResponse<StockMovementResponse>> findAll(

            @Parameter(
                    description = "Unique identifier of the stock item"
            )
            @PathVariable
            UUID stockItemId,


            @Parameter(
                    description = "Optional movement type"
            )
            @RequestParam(required = false)
            StockMovementType type,


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


            @RequestParam(defaultValue = "0")
            int page,


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