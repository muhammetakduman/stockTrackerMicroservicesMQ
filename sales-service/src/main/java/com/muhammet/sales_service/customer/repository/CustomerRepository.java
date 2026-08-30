package com.muhammet.sales_service.customer.repository;

import com.muhammet.sales_service.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.rmi.server.UID;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
}
