package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.StockBalanceResponse;
import com.muhammet.inventory_service.stock.service.StockBalanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-balances")
@RequiredArgsConstructor
@Tag(
        name = "Stock Balances",
        description = "Stock balance query operations"
)
public class StockBalanceController {

    private final StockBalanceService stockBalanceService;


    @GetMapping
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'STOCK_MANAGER',
            'SALES_USER',
            'PRODUCTION_USER'
        )
        """)
    @Operation(
            summary = "List stock balances",
            description = """
                    Returns all stock balances together with
                    their related stock item information.

                    Results are ordered alphabetically by
                    stock item name.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock balances returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<List<StockBalanceResponse>> findAll() {

        return ResponseEntity.ok(
                stockBalanceService.findAll()
        );
    }
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(
            summary = "List low stock items",
            description = """
                Returns active stock items whose available quantity
                is less than or equal to the specified threshold.

                Available quantity is calculated as:

                onHandQuantity - reservedQuantity
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Low stock balances returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid threshold"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<List<StockBalanceResponse>> findLowStock(

            @Parameter(
                    description = "Maximum available quantity considered low stock",
                    example = "10"
            )
            @RequestParam
            BigDecimal threshold
    ) {

        return ResponseEntity.ok(
                stockBalanceService.findLowStock(
                        threshold
                )
        );
    }
}