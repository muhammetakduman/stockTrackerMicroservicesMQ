package com.muhammet.inventory_service.stock.controller;

import com.muhammet.inventory_service.stock.dto.CreateStockItemRequest;
import com.muhammet.inventory_service.stock.dto.StockItemResponse;
import com.muhammet.inventory_service.stock.dto.UpdateStockItemRequest;
import com.muhammet.inventory_service.stock.dto.UpdateStockItemStatusRequest;
import com.muhammet.inventory_service.stock.enums.StockItemType;
import com.muhammet.inventory_service.stock.enums.StockUnit;
import com.muhammet.inventory_service.stock.service.StockItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/v1/stock-items")
@RequiredArgsConstructor
@Tag(
        name = "Stock Items",
        description = "Stock item creation and query operations"
)
public class StockItemController {

    private final StockItemService stockItemService;


    // =========================================================
    // CREATE STOCK ITEM
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN') or hasRole('STOCK_MANAGER')")
    @Operation(
            summary = "Create a stock item",
            description = """
                    Creates a new stock item and automatically creates
                    an empty stock balance for that item.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Stock item created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A stock item with the same SKU already exists"
            )
    })
    public ResponseEntity<StockItemResponse> create(
            @Valid
            @RequestBody
            CreateStockItemRequest request
    ) {

        StockItemResponse response =
                stockItemService.create(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // =========================================================
    // GET ALL STOCK ITEMS
    // =========================================================

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
            summary = "List stock items",
            description = """
                Returns stock items with optional filtering.

                Supported filters:
                - active
                - type
                - unit

                Filters can be used individually or together.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock items returned successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    public ResponseEntity<List<StockItemResponse>> findAll(

            @Parameter(
                    description = "Filter by active status"
            )
            @RequestParam(required = false)
            Boolean active,

            @Parameter(
                    description = "Filter by stock item type"
            )
            @RequestParam(required = false)
            StockItemType type,

            @Parameter(
                    description = "Filter by stock unit"
            )
            @RequestParam(required = false)
            StockUnit unit
    ) {

        return ResponseEntity.ok(
                stockItemService.findAll(
                        active,
                        type,
                        unit
                )
        );
    }


    // =========================================================
    // GET STOCK ITEM BY ID
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize("""
        hasAnyRole(
            'ADMIN',
            'STOCK_MANAGER',
            'SALES_USER',
            'PRODUCTION_USER'
        )
        """)
    @Operation(
            summary = "Get stock item by ID",
            description = """
                    Returns a stock item and its current stock balance
                    using the stock item's unique identifier.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock item found"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Stock item not found"
            )
    })
    public ResponseEntity<StockItemResponse> findById(

            @Parameter(
                    description = "Unique identifier of the stock item",
                    required = true
            )
            @PathVariable
            UUID id
    ) {

        return ResponseEntity.ok(
                stockItemService.findById(
                        id
                )
        );
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(
            summary = "Update stock item status",
            description = """
                Activates or deactivates a stock item.

                Deactivating a stock item does not delete its
                historical stock movements, purchases or sales.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock item status updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Stock item not found"
            )
    })
    public ResponseEntity<StockItemResponse> updateStatus(

            @Parameter(
                    description = "Unique identifier of the stock item",
                    required = true
            )
            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateStockItemStatusRequest request
    ) {

        return ResponseEntity.ok(
                stockItemService.updateStatus(
                        id,
                        request.active()
                )
        );
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCK_MANAGER')")
    @Operation(
            summary = "Update stock item",
            description = """
                Partially updates a stock item's editable information.

                Editable fields:
                - name
                - description

                SKU, item type and unit cannot be changed through
                this endpoint.

                Active status is managed through the dedicated
                /status endpoint.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock item updated successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid update request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Stock item not found"
            )
    })
    public ResponseEntity<StockItemResponse> update(

            @Parameter(
                    description = "Unique identifier of the stock item",
                    required = true
            )
            @PathVariable
            UUID id,

            @RequestBody
            UpdateStockItemRequest request
    ) {

        return ResponseEntity.ok(
                stockItemService.update(
                        id,
                        request
                )
        );
    }
}