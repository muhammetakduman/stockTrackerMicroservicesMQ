package com.muhammet.sales_service.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VoidPaymentRequest(

        @NotBlank
        @Size(max = 500)
        String reason

) {
}