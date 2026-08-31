package com.muhammet.sales_service.sale.service;


import com.muhammet.sales_service.customer.domain.Customer;
import com.muhammet.sales_service.customer.repository.CustomerRepository;
import com.muhammet.sales_service.outbox.service.SalesOutboxService;
import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.domain.SaleStatus;
import com.muhammet.sales_service.sale.dto.request.CreateSaleRequest;
import com.muhammet.sales_service.sale.dto.response.PageResponse;
import com.muhammet.sales_service.sale.dto.response.SaleResponse;
import com.muhammet.sales_service.sale.messaging.event.SaleCreatedEvent;
import com.muhammet.sales_service.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.muhammet.sales_service.customer.domain.Customer;
import com.muhammet.sales_service.customer.repository.CustomerRepository;

import com.muhammet.sales_service.sale.specification.SaleSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;


import java.time.Instant;
import java.util.UUID;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {



    private final CustomerRepository customerRepository;

    private final SaleRepository
            saleRepository;

    private final SalesOutboxService
            salesOutboxService;


    @Transactional
    public SaleResponse createSale(
            UUID sellerId,
            CreateSaleRequest request
    ) {

        Objects.requireNonNull(
                sellerId,
                "Seller ID cannot be null"
        );

        Objects.requireNonNull(
                request,
                "Create sale request cannot be null"
        );


        /*
         * 1. Domain entity oluştur.
         */
        Customer customer =
                customerRepository
                        .findById(
                                request.customerId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Customer not found: "
                                                + request.customerId()
                                )
                        );


        if (!customer.isActive()) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer is inactive"
            );
        }
        Sale sale =
                Sale.create(
                        sellerId,

                        customer.getId(),
                        customer.getFullName(),

                        request.stockItemId(),
                        request.quantity(),
                        request.unitPrice(),
                        request.soldAt(),

                        request.note()
                );


        /*
         * 2. Database'e gönder.
         *
         * ID oluşturulması için flush ediyoruz.
         */
        Sale savedSale =
                saleRepository.saveAndFlush(
                        sale
                );


        /*
         * 3. Inventory'ye gönderilecek event oluştur.
         */
        SaleCreatedEvent event =
                SaleCreatedEvent.from(
                        savedSale
                );


        /*
         * 4. RabbitMQ'ya DIRECT publish yok.
         *
         * Event aynı transaction içinde
         * Outbox'a kaydedilir.
         */
        salesOutboxService.append(
                event
        );


        log.info(
                "Sale and outbox event created. " +
                        "saleId={}, eventId={}, sellerId={}, " +
                        "stockItemId={}, quantity={}, status={}",
                savedSale.getId(),
                event.eventId(),
                savedSale.getSellerId(),
                savedSale.getStockItemId(),
                savedSale.getQuantity(),
                savedSale.getStatus()
        );


        return SaleResponse.from(
                savedSale
        );
    }
    @Transactional(readOnly = true)
    public PageResponse<SaleResponse> findAll(
            SaleStatus status,
            UUID sellerId,
            UUID stockItemId,
            Instant from,
            Instant to,
            int page,
            int size
    ) {

        validatePagination(
                page,
                size
        );

        validateDateRange(
                from,
                to
        );


        Specification<Sale> specification =
                SaleSpecification
                        .hasStatus(status)
                        .and(
                                SaleSpecification
                                        .hasSellerId(sellerId)
                        )
                        .and(
                                SaleSpecification
                                        .hasStockItemId(stockItemId)
                        )
                        .and(
                                SaleSpecification
                                        .soldFrom(from)
                        )
                        .and(
                                SaleSpecification
                                        .soldTo(to)
                        );


        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "soldAt"
                        ).and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                );


        Page<Sale> result =
                saleRepository.findAll(
                        specification,
                        pageable
                );


        return new PageResponse<>(
                result.getContent()
                        .stream()
                        .map(SaleResponse::from)
                        .toList(),

                result.getNumber(),
                result.getSize(),

                result.getTotalElements(),
                result.getTotalPages(),

                result.isFirst(),
                result.isLast()
        );
    }

    private void validatePagination(
            int page,
            int size
    ) {

        if (page < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page cannot be negative"
            );
        }

        if (size < 1 ||
                size > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size must be between 1 and 100"
            );
        }
    }


    private void validateDateRange(
            Instant from,
            Instant to
    ) {

        if (from != null &&
                to != null &&
                from.isAfter(to)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From date cannot be after to date"
            );
        }
    }
    @Transactional(readOnly = true)
    public SaleResponse findById(
            Long saleId,
            UUID currentSellerId,
            boolean isAdmin
    ) {

        if (saleId == null ||
                saleId <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sale ID must be greater than zero"
            );
        }


        Sale sale =
                saleRepository.findById(
                                saleId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Sale not found: " + saleId
                                )
                        );

        /*
         * SALES_USER yalnızca kendi satışını görebilir.
         * ADMIN her satışı görebilir.
         */
        if (!isAdmin && !sale.getSellerId().equals(currentSellerId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: this sale belongs to another seller"
            );
        }

        return SaleResponse.from(
                sale
        );
    }
}