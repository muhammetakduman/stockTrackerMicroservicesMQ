package com.muhammet.sales_service.sale.service;


import com.muhammet.sales_service.outbox.service.SalesOutboxService;
import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.dto.request.CreateSaleRequest;
import com.muhammet.sales_service.sale.dto.request.SaleResponse;
import com.muhammet.sales_service.sale.messaging.event.SaleCreatedEvent;
import com.muhammet.sales_service.sale.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository
            saleRepository;

    private final SalesOutboxService
            salesOutboxService;


    @Transactional
    public SaleResponse createSale(
            CreateSaleRequest request
    ) {

        Objects.requireNonNull(
                request,
                "Create sale request cannot be null"
        );


        /*
         * 1. Domain entity oluştur.
         */
        Sale sale =
                Sale.create(
                        request.sellerId(),
                        request.stockItemId(),
                        request.quantity(),
                        request.unitPrice(),
                        request.soldAt()
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
}