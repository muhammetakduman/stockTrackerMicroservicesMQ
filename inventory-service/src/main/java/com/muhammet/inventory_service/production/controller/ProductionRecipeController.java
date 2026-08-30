package com.muhammet.inventory_service.production.controller;

import com.muhammet.inventory_service.production.dto.request.CreateProductionRecipeRequest;
import com.muhammet.inventory_service.production.dto.request.UpdateProductionRecipeStatusRequest;
import com.muhammet.inventory_service.production.dto.response.ProductionRecipeResponse;
import com.muhammet.inventory_service.production.service.ProductionRecipeService;

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
        "/api/v1/production-recipes"
)
@RequiredArgsConstructor
@Tag(
        name = "Production Recipes",
        description = "Perfume production recipe operations"
)
public class ProductionRecipeController {

    private final ProductionRecipeService
            productionRecipeService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'STOCK_MANAGER')"
    )
    @Operation(
            summary = "Create production recipe",
            description = """
                    Defines how an essence, bottle and packaging
                    set are converted into a finished perfume.

                    Production quantities are later calculated
                    from this recipe by the backend.
                    """
    )
    public ResponseEntity<ProductionRecipeResponse> create(

            @Valid
            @RequestBody
            CreateProductionRecipeRequest request
    ) {

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        productionRecipeService.create(
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
            summary = "List production recipes"
    )
    public ResponseEntity<List<ProductionRecipeResponse>> findAll() {

        return ResponseEntity.ok(
                productionRecipeService.findAll()
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
            summary = "Get production recipe by ID"
    )
    public ResponseEntity<ProductionRecipeResponse> findById(

            @PathVariable
            UUID id
    ) {

        return ResponseEntity.ok(
                productionRecipeService.findById(
                        id
                )
        );
    }


    // =========================================================
    // STATUS
    // =========================================================

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'STOCK_MANAGER')"
    )
    @Operation(
            summary = "Activate or deactivate production recipe"
    )
    public ResponseEntity<ProductionRecipeResponse> updateStatus(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateProductionRecipeStatusRequest request
    ) {

        return ResponseEntity.ok(
                productionRecipeService.updateStatus(
                        id,
                        request.active()
                )
        );
    }
}