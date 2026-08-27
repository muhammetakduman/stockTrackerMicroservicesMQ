package com.muhammet.sales_service.sale.controller;

import com.muhammet.sales_service.sale.domain.SaleStatus;
import com.muhammet.sales_service.sale.dto.request.CreateSaleRequest;
import com.muhammet.sales_service.sale.dto.response.PageResponse;
import com.muhammet.sales_service.sale.dto.response.SaleResponse;
import com.muhammet.sales_service.sale.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(
        name = "Sales",
        description = "Sale creation and query operations"
)
public class SaleController {

    private final SaleService saleService;


    // =========================================================
    // CREATE SALE
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create sale",
            description = """
                    Creates a sale in PENDING_STOCK_UPDATE status.

                    The sale.created event is stored in the
                    transactional outbox and inventory processing
                    continues asynchronously.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Sale created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid sale request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public SaleResponse createSale(
            @Valid
            @RequestBody
            CreateSaleRequest request
    ) {

        return saleService.createSale(
                request
        );
    }


    // =========================================================
    // LIST SALES
    // =========================================================

    @GetMapping
    @Operation(
            summary = "List sales",
            description = """
                    Returns paginated sales.

                    Optional filters:
                    - status
                    - sellerId
                    - stockItemId
                    - soldAt date range
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sales returned successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter or pagination value"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<PageResponse<SaleResponse>> findAll(

            @Parameter(
                    description = "Filter by sale status"
            )
            @RequestParam(required = false)
            SaleStatus status,


            @Parameter(
                    description = "Filter by seller ID"
            )
            @RequestParam(required = false)
            Long sellerId,


            @Parameter(
                    description = "Filter by stock item ID"
            )
            @RequestParam(required = false)
            UUID stockItemId,


            @Parameter(
                    description = "Beginning of soldAt date range",
                    example = "2026-08-01T00:00:00Z"
            )
            @RequestParam(required = false)
            Instant from,


            @Parameter(
                    description = "End of soldAt date range",
                    example = "2026-08-31T23:59:59Z"
            )
            @RequestParam(required = false)
            Instant to,


            @Parameter(
                    description = "Zero-based page number",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,


            @Parameter(
                    description = "Number of records per page",
                    example = "20"
            )
            @RequestParam(defaultValue = "20")
            int size
    ) {

        return ResponseEntity.ok(
                saleService.findAll(
                        status,
                        sellerId,
                        stockItemId,
                        from,
                        to,
                        page,
                        size
                )
        );
    }


    // =========================================================
    // GET SALE BY ID
    // =========================================================

    @GetMapping("/{id}")
    @Operation(
            summary = "Get sale by ID",
            description = """
                    Returns detailed information about
                    a single sale.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sale found"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid sale ID"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Sale not found"
            )
    })
    public ResponseEntity<SaleResponse> findById(

            @Parameter(
                    description = "Unique identifier of the sale",
                    required = true
            )
            @PathVariable
            Long id
    ) {

        return ResponseEntity.ok(
                saleService.findById(
                        id
                )
        );
    }
}