package com.muhammet.purchase_service.purchase.controller;

import com.muhammet.purchase_service.purchase.dto.request.CreatePurchaseRequest;
import com.muhammet.purchase_service.purchase.dto.response.PurchaseResponse;
import com.muhammet.purchase_service.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
public class PurchaseController {
    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody CreatePurchaseRequest request){
        PurchaseResponse response = purchaseService.createPurchase(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping("/{id}")
    public  ResponseEntity<PurchaseResponse> getPurchaseById(
            @PathVariable Long id
    ){
        PurchaseResponse response = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(response);
    }
}
