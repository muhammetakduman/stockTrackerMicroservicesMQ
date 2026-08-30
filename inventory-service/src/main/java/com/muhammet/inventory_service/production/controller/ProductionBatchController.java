package com.muhammet.inventory_service.production.controller;

import com.muhammet.inventory_service.production.dto.request.CreateProductionBatchRequest;
import com.muhammet.inventory_service.production.dto.response.ProductionBatchResponse;
import com.muhammet.inventory_service.production.service.ProductionBatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/production-batches"
)
@RequiredArgsConstructor
@Tag(
        name = "Production Batches",
        description = "Perfume production operations"
)
public class ProductionBatchController {

    private final ProductionBatchService
            productionBatchService;


    // =========================================================
    // PRODUCE
    // =========================================================

    @PostMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'STOCK_MANAGER',
                'PRODUCTION_USER'
            )
            """)
    @Operation(
            summary = "Produce finished perfume",
            description = """
                    Consumes essence, bottle and packaging stock
                    according to the selected production recipe
                    and increases finished product stock.

                    All stock changes are committed atomically.
                    """
    )
    public ResponseEntity<ProductionBatchResponse> create(

            @Valid
            @RequestBody
            CreateProductionBatchRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        productionBatchService.create(
                                request
                        )
                );
    }


    // =========================================================
    // LIST
    // =========================================================

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'STOCK_MANAGER',
                'PRODUCTION_USER',
                'SALES_USER'
            )
            """)
    @Operation(
            summary = "List production history"
    )
    public ResponseEntity<List<ProductionBatchResponse>> findAll() {

        return ResponseEntity.ok(
                productionBatchService.findAll()
        );
    }


    // =========================================================
    // DETAIL
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'STOCK_MANAGER',
                'PRODUCTION_USER',
                'SALES_USER'
            )
            """)
    @Operation(
            summary = "Get production batch by ID"
    )
    public ResponseEntity<ProductionBatchResponse> findById(

            @PathVariable
            UUID id
    ) {

        return ResponseEntity.ok(
                productionBatchService.findById(
                        id
                )
        );
    }
}