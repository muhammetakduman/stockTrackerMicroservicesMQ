package com.muhammet.purchase_service.purchase.controller;

import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;
import com.muhammet.purchase_service.purchase.dto.request.CreatePurchaseRequest;
import com.muhammet.purchase_service.purchase.dto.response.PageResponse;
import com.muhammet.purchase_service.purchase.dto.response.PurchaseResponse;
import com.muhammet.purchase_service.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(
        name = "Purchases",
        description = "Purchase creation and query oprations"
)
public class PurchaseController {
    private final PurchaseService purchaseService;

    @Operation(
            summary = "Create purchase",
            description = """
                Creates a purchase in PENDING_STOCK_UPDATE status.

                After creation, a purchase.created event is written
                to the transactional outbox and inventory stock
                processing continues asynchronously.
                """
    )
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

    @Operation(
            summary = "Get purchase by ID",
            description = "Returns the details of a single purchase."
    )
    @GetMapping("/{id}")
    public  ResponseEntity<PurchaseResponse> getPurchaseById(
            @PathVariable Long id
    ){
        PurchaseResponse response = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List purchases",
            description = """
                Returns paginated purchases.

                Optional filters:
                - status
                - stockItemId
                - supplierName
                - purchasedAt date range
                """
    )
    @GetMapping
    public ResponseEntity<PageResponse<PurchaseResponse>> findAll(

            @RequestParam(required = false)
            PurchaseStatus status,

            @RequestParam(required = false)
            UUID stockItemId,

            @RequestParam(required = false)
            String supplierName,

            @RequestParam(required = false)
            Instant from,

            @RequestParam(required = false)
            Instant to,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                purchaseService.findAll(
                        status,
                        stockItemId,
                        supplierName,
                        from,
                        to,
                        page,
                        size
                )
        );
    }
}
