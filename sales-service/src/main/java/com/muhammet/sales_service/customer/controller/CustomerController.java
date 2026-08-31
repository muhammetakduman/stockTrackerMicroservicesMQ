package com.muhammet.sales_service.customer.controller;


import com.muhammet.sales_service.customer.domain.Customer;
import com.muhammet.sales_service.customer.dto.request.CreateCustomerRequest;
import com.muhammet.sales_service.customer.dto.request.UpdateCustomerRequest;
import com.muhammet.sales_service.customer.dto.request.UpdateCustomerStatusRequest;
import com.muhammet.sales_service.customer.dto.response.CustomerResponse;
import com.muhammet.sales_service.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer", description = "Customer managemend for sales")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_USER')")
    @Operation(summary = "Create customer")
    @ApiResponse(responseCode = "201" , description = "Customer created")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request){
        return  ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES_USER')")
    @Operation(summary = "Search customers")
    public ResponseEntity<List<CustomerResponse>> search(@RequestParam(required = false) String query){
        return ResponseEntity.ok(customerService.search(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_USER')")
    @Operation(summary = "Get customer by id")
    public ResponseEntity<CustomerResponse>findById(@PathVariable UUID id){
        return ResponseEntity.ok(customerService.findById(id));
    }

    @PutMapping ("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_USER')")
    @Operation(summary = "Update customer by id")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateCustomerRequest request){
        return ResponseEntity.ok(customerService.update(id,request));
    }
    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'SALES_USER')"
    )
    @Operation(
            summary = "Activate or deactivate customer"
    )
    public ResponseEntity<CustomerResponse> updateStatus(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            UpdateCustomerStatusRequest request
    ) {

        return ResponseEntity.ok(
                customerService.updateStatus(
                        id,
                        request.active()
                )
        );
    }

}
