package com.muhammet.sales_service.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank(
                message = "Customer name must not be blank"
        )
        @Size(max = 100)
        String fullName,

        @Size(max = 1000)
        String notes
) {
}
