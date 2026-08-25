package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.AdjustStockRequest;
import com.muhammet.inventory_service.stock.dto.StockAdjustmentResponse;
import com.muhammet.inventory_service.stock.service.StockAdjustmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/stock-items/{stockItemId}/adjustments"
)
@RequiredArgsConstructor
@Tag(
        name = "Stock Adjustments",
        description = "Manual stock correction operations"
)
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;


    @PostMapping
    @Operation(
            summary = "Adjust stock quantity",
            description = """
                    Changes the on-hand quantity to the requested
                    target value and records the difference as
                    an ADJUSTMENT stock movement.
                    """
    )
    public ResponseEntity<StockAdjustmentResponse> adjust(

            @PathVariable
            UUID stockItemId,

            @Valid
            @RequestBody
            AdjustStockRequest request
    ) {

        return ResponseEntity.ok(
                stockAdjustmentService.adjust(
                        stockItemId,
                        request
                )
        );
    }
}