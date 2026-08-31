package com.muhammet.sales_service.customer.service;


import com.muhammet.sales_service.customer.domain.Customer;
import com.muhammet.sales_service.customer.dto.request.CreateCustomerRequest;
import com.muhammet.sales_service.customer.dto.request.UpdateCustomerRequest;
import com.muhammet.sales_service.customer.dto.response.CustomerResponse;
import com.muhammet.sales_service.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse create(
            CreateCustomerRequest request
    ){
        Customer customer = Customer.create(
                        request.fullName(),
                        request.notes()
                );
        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }
    @Transactional(readOnly = true)
    public CustomerResponse findById(
            UUID id
    ){
        return CustomerResponse.from(
                getCustomer(id)
        );
    }
    @Transactional(readOnly = true)
    public List<CustomerResponse> search(String query){
        List<Customer> customers;
        if(query == null || query.isBlank()){
            customers = customerRepository.findAllByActiveTrueOrderByFullNameAsc();
        } else {
            customers = customerRepository.findTopByActiveTrueAndFullNameContainingIgnoreCaseOrderByFullNameAsc(query.trim());
        }
        return customers .stream().map(CustomerResponse::from)
                .toList();
    }
    @Transactional
    public CustomerResponse update (UUID id, UpdateCustomerRequest request){
        Customer customer = getCustomer(id);
        customer.update(
                request.fullName(),
                request.notes()
        );
        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse updadateStatus(UUID id , boolean active){
        Customer customer = getCustomer(id);
        if(active){
            customer.activate();
        } else {
            customer.deactivate();
        }
        return CustomerResponse.from(customer);
    }


    private Customer getCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(()->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found: "+ id));
    }
}
