package com.muhammet.sales_service.customer.repository;

import com.muhammet.sales_service.customer.domain.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository
        extends JpaRepository<Customer, UUID> {

    List<Customer>
    findTop20ByActiveTrueAndFullNameContainingIgnoreCaseOrderByFullNameAsc(
            String fullName
    );

    List<Customer>
    findAllByActiveTrueOrderByFullNameAsc();
}