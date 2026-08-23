package com.muhammet.sales_service.sale.service;

import com.muhammet.sales_service.inbox.service.ProcessedEventService;
import com.muhammet.sales_service.sale.domain.Sale;
import com.muhammet.sales_service.sale.messaging.event.StockDecreaseCompletedEvent;
import com.muhammet.sales_service.sale.messaging.event.StockDecreaseFailedEvent;
import com.muhammet.sales_service.sale.repository.SaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleStockResultService {

    private static final String COMPLETED_CONSUMER =
            "sales.stock-decrease-completed";

    private static final String FAILED_CONSUMER =
            "sales.stock-decrease-failed";

    private static final String COMPLETED_EVENT_TYPE =
            "stock.decrease.completed";

    private static final String FAILED_EVENT_TYPE =
            "stock.decrease.failed";

    private static final int SUPPORTED_EVENT_VERSION = 1;

    private static final int MAX_FAILURE_REASON_LENGTH = 500;


    private final ProcessedEventService
            processedEventService;

    private final SaleRepository
            saleRepository;


    // =========================================================
    // STOCK DECREASE COMPLETED
    // =========================================================

    @Transactional
    public void completeStockUpdate(
            StockDecreaseCompletedEvent event
    ) {

        validateCompletedEvent(
                event
        );


        /*
         * 1. INBOX
         */
        boolean firstProcessing =
                processedEventService.tryRegister(
                        event.eventId(),
                        event.eventType(),
                        COMPLETED_CONSUMER
                );


        if (!firstProcessing) {

            log.info(
                    "Duplicate StockDecreaseCompletedEvent ignored. " +
                            "eventId={}, saleId={}",
                    event.eventId(),
                    event.saleId()
            );

            return;
        }


        /*
         * 2. SALE LOCK
         */
        Sale sale =
                saleRepository
                        .findByIdForUpdate(
                                event.saleId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Sale not found. saleId="
                                                + event.saleId()
                                )
                        );


        /*
         * 3. EVENT İLE SALE AYNI İŞLEM Mİ?
         */
        validateCompletedEventMatchesSale(
                sale,
                event
        );


        /*
         * 4. DOMAIN STATE TRANSITION
         *
         * PENDING_STOCK_UPDATE
         *          ↓
         *      COMPLETED
         */
        sale.markStockUpdateCompleted();


        /*
         * save() şart değil.
         *
         * Sale managed entity olduğu için Hibernate
         * dirty checking ile UPDATE yapacak.
         */


        log.info(
                "Sale stock update completed. " +
                        "saleId={}, eventId={}, stockItemId={}, " +
                        "quantityDecreased={}, stockMovementId={}, " +
                        "newOnHandQuantity={}",
                sale.getId(),
                event.eventId(),
                event.stockItemId(),
                event.quantityDecreased(),
                event.stockMovementId(),
                event.newOnHandQuantity()
        );
    }


    // =========================================================
    // STOCK DECREASE FAILED
    // =========================================================

    @Transactional
    public void failStockUpdate(
            StockDecreaseFailedEvent event
    ) {

        validateFailedEvent(
                event
        );


        /*
         * 1. INBOX
         */
        boolean firstProcessing =
                processedEventService.tryRegister(
                        event.eventId(),
                        event.eventType(),
                        FAILED_CONSUMER
                );


        if (!firstProcessing) {

            log.info(
                    "Duplicate StockDecreaseFailedEvent ignored. " +
                            "eventId={}, saleId={}",
                    event.eventId(),
                    event.saleId()
            );

            return;
        }


        /*
         * 2. SALE LOCK
         */
        Sale sale =
                saleRepository
                        .findByIdForUpdate(
                                event.saleId()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Sale not found. saleId="
                                                + event.saleId()
                                )
                        );


        /*
         * 3. EVENT İLE SALE EŞLEŞMESİ
         */
        validateFailedEventMatchesSale(
                sale,
                event
        );


        /*
         * DB column:
         *
         * failure_reason VARCHAR(500)
         *
         * olduğundan güvenli şekilde truncate ediyoruz.
         */
        String failureReason =
                buildFailureReason(
                        event.errorCode(),
                        event.failureReason()
                );


        /*
         * PENDING_STOCK_UPDATE
         *          ↓
         *        FAILED
         */
        sale.markStockUpdateFailed(
                failureReason
        );


        log.warn(
                "Sale stock update failed. " +
                        "saleId={}, eventId={}, stockItemId={}, " +
                        "errorCode={}, failureReason={}",
                sale.getId(),
                event.eventId(),
                event.stockItemId(),
                event.errorCode(),
                failureReason
        );
    }


    // =========================================================
    // COMPLETED EVENT VALIDATION
    // =========================================================

    private void validateCompletedEvent(
            StockDecreaseCompletedEvent event
    ) {

        Objects.requireNonNull(
                event,
                "Stock decrease completed event cannot be null"
        );

        Objects.requireNonNull(
                event.eventId(),
                "Event ID cannot be null"
        );

        Objects.requireNonNull(
                event.sourceEventId(),
                "Source event ID cannot be null"
        );

        Objects.requireNonNull(
                event.saleId(),
                "Sale ID cannot be null"
        );

        Objects.requireNonNull(
                event.stockItemId(),
                "Stock item ID cannot be null"
        );

        Objects.requireNonNull(
                event.stockMovementId(),
                "Stock movement ID cannot be null"
        );

        Objects.requireNonNull(
                event.quantityDecreased(),
                "Quantity decreased cannot be null"
        );

        Objects.requireNonNull(
                event.newOnHandQuantity(),
                "New on-hand quantity cannot be null"
        );


        if (!COMPLETED_EVENT_TYPE.equals(
                event.eventType()
        )) {

            throw new IllegalArgumentException(
                    "Unsupported completed event type: "
                            + event.eventType()
            );
        }


        if (event.eventVersion()
                != SUPPORTED_EVENT_VERSION) {

            throw new IllegalArgumentException(
                    "Unsupported completed event version: "
                            + event.eventVersion()
            );
        }


        if (event.saleId() <= 0) {

            throw new IllegalArgumentException(
                    "Sale ID must be greater than zero"
            );
        }


        if (event.quantityDecreased()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Quantity decreased must be greater than zero"
            );
        }


        if (event.newOnHandQuantity()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "New on-hand quantity cannot be negative"
            );
        }
    }


    // =========================================================
    // FAILED EVENT VALIDATION
    // =========================================================

    private void validateFailedEvent(
            StockDecreaseFailedEvent event
    ) {

        Objects.requireNonNull(
                event,
                "Stock decrease failed event cannot be null"
        );

        Objects.requireNonNull(
                event.eventId(),
                "Event ID cannot be null"
        );

        Objects.requireNonNull(
                event.sourceEventId(),
                "Source event ID cannot be null"
        );

        Objects.requireNonNull(
                event.saleId(),
                "Sale ID cannot be null"
        );

        Objects.requireNonNull(
                event.stockItemId(),
                "Stock item ID cannot be null"
        );


        if (!FAILED_EVENT_TYPE.equals(
                event.eventType()
        )) {

            throw new IllegalArgumentException(
                    "Unsupported failed event type: "
                            + event.eventType()
            );
        }


        if (event.eventVersion()
                != SUPPORTED_EVENT_VERSION) {

            throw new IllegalArgumentException(
                    "Unsupported failed event version: "
                            + event.eventVersion()
            );
        }


        if (event.saleId() <= 0) {

            throw new IllegalArgumentException(
                    "Sale ID must be greater than zero"
            );
        }


        if (event.errorCode() == null ||
                event.errorCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Error code cannot be blank"
            );
        }


        if (event.failureReason() == null ||
                event.failureReason().isBlank()) {

            throw new IllegalArgumentException(
                    "Failure reason cannot be blank"
            );
        }
    }


    // =========================================================
    // CROSS CHECK
    // =========================================================

    private void validateCompletedEventMatchesSale(
            Sale sale,
            StockDecreaseCompletedEvent event
    ) {

        /*
         * Inventory başka stock item'ın sonucunu
         * yanlış sale'e bağlamış olmasın.
         */
        if (!sale.getStockItemId()
                .equals(event.stockItemId())) {

            throw new IllegalStateException(
                    "Stock item mismatch. saleId="
                            + sale.getId()
                            + ", expectedStockItemId="
                            + sale.getStockItemId()
                            + ", receivedStockItemId="
                            + event.stockItemId()
            );
        }


        /*
         * Sale quantity ile inventory'nin gerçekten
         * azalttığını söylediği quantity aynı olmalı.
         *
         * BigDecimal equals kullanmıyoruz:
         *
         * 5.0
         * 5.000
         *
         * business açısından aynıdır.
         */
        if (sale.getQuantity()
                .compareTo(
                        event.quantityDecreased()
                ) != 0) {

            throw new IllegalStateException(
                    "Sale quantity mismatch. saleId="
                            + sale.getId()
                            + ", expectedQuantity="
                            + sale.getQuantity()
                            + ", decreasedQuantity="
                            + event.quantityDecreased()
            );
        }
    }


    private void validateFailedEventMatchesSale(
            Sale sale,
            StockDecreaseFailedEvent event
    ) {

        if (!sale.getStockItemId()
                .equals(event.stockItemId())) {

            throw new IllegalStateException(
                    "Stock item mismatch for failed sale. saleId="
                            + sale.getId()
                            + ", expectedStockItemId="
                            + sale.getStockItemId()
                            + ", receivedStockItemId="
                            + event.stockItemId()
            );
        }
    }


    // =========================================================
    // FAILURE REASON
    // =========================================================

    private String buildFailureReason(
            String errorCode,
            String failureReason
    ) {

        String result =
                errorCode.trim()
                        + ": "
                        + failureReason.trim();


        if (result.length()
                <= MAX_FAILURE_REASON_LENGTH) {

            return result;
        }


        return result.substring(
                0,
                MAX_FAILURE_REASON_LENGTH
        );
    }
}