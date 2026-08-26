package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.StockBalanceResponse;
import com.muhammet.inventory_service.stock.service.StockBalanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}