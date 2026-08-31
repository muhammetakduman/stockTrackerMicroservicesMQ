package com.muhammet.sales_service.customer.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(

        @NotNull(
                message = "Active status is required"
        )
        Boolean active

) {
}