package com.muhammet.sales_service.customer.dto.response;

import com.muhammet.sales_service.customer.domain.Customer;

import java.time.Instant;
import java.util.UUID;


public record CustomerResponse(

        UUID id,
        String fullName,
        String notes,
        boolean active,
        Instant ceatedAt,
        Instant updatedAt

) {
    public static CustomerResponse from(
            Customer  customer
    ){
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getNotes(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
