package com.muhammet.inventory_service.stock.controller;


import com.muhammet.inventory_service.stock.dto.CreateStockItemRequest;
import com.muhammet.inventory_service.stock.dto.StockItemResponse;
import com.muhammet.inventory_service.stock.service.StockItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-items")
@RequiredArgsConstructor
public class StockItemController {
    private final StockItemService stockItemService;

    @PostMapping
    public ResponseEntity<StockItemResponse>create(
            @Valid @RequestBody CreateStockItemRequest request
            ) {
        StockItemResponse response = stockItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
    @GetMapping
    public ResponseEntity<StockItemResponse> findById(
            @PathVariable UUID id
            ){
        return ResponseEntity.ok(stockItemService.findById(id));
    }
}
