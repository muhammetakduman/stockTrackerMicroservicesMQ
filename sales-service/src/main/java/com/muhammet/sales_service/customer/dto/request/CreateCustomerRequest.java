package com.muhammet.sales_service.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(

        @NotBlank(
                message = "Customer full name is required"
        )
        @Size(
                max = 150,
                message = "Customer full name cannot exceed 150 characters"
        )
        String fullName,

        @Size(
                max = 1000,
                message = "Customer notes cannot exceed 1000 characters"
        )
        String notes

) {
}