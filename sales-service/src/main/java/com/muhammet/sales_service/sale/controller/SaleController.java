package com.muhammet.sales_service.sale.controller;

import com.muhammet.sales_service.sale.dto.request.CreateSaleRequest;
import com.muhammet.sales_service.sale.dto.request.SaleResponse;
import com.muhammet.sales_service.sale.service.SaleService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(
            @Valid @RequestBody CreateSaleRequest request
    ) {

        return saleService.createSale(request);
    }
}