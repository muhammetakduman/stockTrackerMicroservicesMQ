package com.muhammet.sales_service.sale.controller;

import com.muhammet.sales_service.sale.domain.SaleStatus;
import com.muhammet.sales_service.sale.dto.request.CreateSaleRequest;
import com.muhammet.sales_service.sale.dto.response.PageResponse;
import com.muhammet.sales_service.sale.dto.response.SaleResponse;
import com.muhammet.sales_service.sale.security.AuthenticatedSeller;
import com.muhammet.sales_service.sale.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final AuthenticatedSeller authenticatedSeller;


    // =========================================================
    // CREATE SALE
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SALES_USER', 'ADMIN')")
    @Operation(
            summary = "Create sale",
            description = """
                    Creates a sale in PENDING_STOCK_UPDATE status.

                    sellerId is resolved automatically from the JWT subject claim.
                    Do NOT send sellerId in the request body.

                    The sale.created event is stored in the
                    transactional outbox and inventory processing
                    continues asynchronously.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
        UUID sellerId = authenticatedSeller.getCurrentSellerId();

        return saleService.createSale(
                sellerId,
                request
        );
    }


    // =========================================================
    // MY SALES — current authenticated user's sales
    // =========================================================

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SALES_USER', 'ADMIN')")
    @Operation(
            summary = "Get my sales",
            description = """
                    Returns paginated sales belonging to the currently
                    authenticated user. sellerId is resolved from the JWT.

                    Optional filters:
                    - status
                    - stockItemId
                    - soldAt date range
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
    public ResponseEntity<PageResponse<SaleResponse>> findMySales(

            @Parameter(description = "Filter by sale status")
            @RequestParam(required = false)
            SaleStatus status,

            @Parameter(description = "Filter by stock item ID")
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

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Number of records per page", example = "20")
            @RequestParam(defaultValue = "20")
            int size
    ) {
        UUID sellerId = authenticatedSeller.getCurrentSellerId();

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
    // LIST SALES
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "List sales",
            description = """
                    Returns paginated sales.

                    Optional filters:
                    - status
                    - sellerId (UUID)
                    - stockItemId
                    - soldAt date range
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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

            @Parameter(description = "Filter by sale status")
            @RequestParam(required = false)
            SaleStatus status,

            @Parameter(description = "Filter by seller UUID")
            @RequestParam(required = false)
            UUID sellerId,

            @Parameter(description = "Filter by stock item ID")
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

            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(description = "Number of records per page", example = "20")
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
    @PreAuthorize("hasAnyRole('SALES_USER', 'ADMIN')")
    @Operation(
            summary = "Get sale by ID",
            description = """
                    Returns detailed information about
                    a single sale.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
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
        UUID currentSellerId = authenticatedSeller.getCurrentSellerId();
        boolean isAdmin = isAdmin();

        return ResponseEntity.ok(
                saleService.findById(
                        id,
                        currentSellerId,
                        isAdmin
                )
        );
    }

    /**
     * SecurityContext'te "ROLE_ADMIN" authority var mı kontrol eder.
     * JwtAuthenticationConverter'ın "roles" claim'ini "ROLE_" prefix ile map ettiği varsayımına dayanır.
     */
    private boolean isAdmin() {
        return org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

