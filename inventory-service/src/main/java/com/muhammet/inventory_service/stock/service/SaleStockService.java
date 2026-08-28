package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.inbox.service.ProcessedEventService;
import com.muhammet.inventory_service.outbox.service.InventoryOutboxService;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.entity.StockMovement;
import com.muhammet.inventory_service.stock.exception.StockProcessingException;
import com.muhammet.inventory_service.stock.messaging.event.SaleCreatedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockDecreaseCompletedEvent;
import com.muhammet.inventory_service.stock.repository.StockBalanceRepository;
import com.muhammet.inventory_service.stock.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleStockService {

    private static final String SALE_CREATED_CONSUMER =
            "inventory.sale-created";

    private static final String SUPPORTED_EVENT_TYPE =
            "sale.created";

    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final ProcessedEventService processedEventService;

    private final InventoryOutboxService inventoryOutboxService;

    private final StockBalanceRepository stockBalanceRepository;

    private final StockMovementRepository stockMovementRepository;


    @Transactional
    public void processSale(
            SaleCreatedEvent event
    ) {

        /*
         * ============================================================
         * 1. EVENT VALIDATION
         * ============================================================
         */

        validateEvent(event);


        /*
         * ============================================================
         * 2. INBOX / IDEMPOTENCY
         * ============================================================
         */

        boolean firstProcessing =
                processedEventService.tryRegister(
                        event.eventId(),
                        event.eventType(),
                        SALE_CREATED_CONSUMER
                );


        if (!firstProcessing) {

            log.info(
                    "Duplicate SaleCreatedEvent ignored by inbox. " +
                            "eventId={}, saleId={}, stockItemId={}",
                    event.eventId(),
                    event.saleId(),
                    event.stockItemId()
            );

            return;
        }


        /*
         * ============================================================
         * 3. STOCK BALANCE + PESSIMISTIC LOCK
         * ============================================================
         *
         * SELECT ... FOR UPDATE
         *
         * Bu StockBalance üzerinde transaction bitene kadar
         * başka satış işleminin aynı anda quantity kontrolü
         * yapmasını engeller.
         */

        StockBalance stockBalance =
                stockBalanceRepository
                        .findByStockItemIdForUpdate(
                                event.stockItemId()
                        )
                        .orElseThrow(() ->
                                new StockProcessingException(
                                        "STOCK_BALANCE_NOT_FOUND",
                                        "Stock balance not found for stock item: "
                                                + event.stockItemId()
                                )
                        );


        /*
         * ============================================================
         * 4. AVAILABLE STOCK
         * ============================================================
         *
         * onHandQuantity doğrudan kullanılmıyor.
         *
         * available =
         * onHandQuantity - reservedQuantity
         */

        BigDecimal availableQuantity =
                stockBalance.getAvailableQuantity();


        /*
         * ============================================================
         * 5. YETERLİ STOK KONTROLÜ
         * ============================================================
         */

        if (availableQuantity.compareTo(
                event.quantity()
        ) < 0) {

            throw new StockProcessingException(
                    "INSUFFICIENT_AVAILABLE_STOCK",
                    "Insufficient available stock. " +
                            "stockItemId=" + event.stockItemId() +
                            ", requestedQuantity=" + event.quantity() +
                            ", availableQuantity=" + availableQuantity
            );
        }


        /*
         * ============================================================
         * 6. PREVIOUS QUANTITY
         * ============================================================
         */

        BigDecimal previousOnHandQuantity =
                stockBalance.getOnHandQuantity();


        /*
         * ============================================================
         * 7. STOCK DECREASE
         * ============================================================
         */

        stockBalance.decreaseOnHandQuantity(
                event.quantity()
        );


        stockBalanceRepository.save(
                stockBalance
        );


        BigDecimal newOnHandQuantity =
                stockBalance.getOnHandQuantity();


        /*
         * ============================================================
         * 8. STOCK MOVEMENT
         * ============================================================
         *
         * Sale quantity event içerisinde pozitiftir:
         *
         * quantity = 5
         *
         * StockMovement.sale() bunu:
         *
         * quantityChange = -5
         *
         * olarak kaydeder.
         */

        StockItem stockItem =
                stockBalance.getStockItem();


        StockMovement movement =
                StockMovement.sale(
                        event.eventId(),
                        stockItem,
                        event.quantity(),
                        previousOnHandQuantity,
                        newOnHandQuantity,
                        event.saleId(),
                        event.occurredAt()
                );


        StockMovement savedMovement =
                stockMovementRepository.save(
                        movement
                );


        /*
         * ============================================================
         * 9. STOCK DECREASE COMPLETED EVENT
         * ============================================================
         */

        StockDecreaseCompletedEvent completedEvent =
                StockDecreaseCompletedEvent.create(
                        event.eventId(),
                        event.saleId(),
                        event.stockItemId(),
                        savedMovement.getId(),
                        event.quantity(),
                        newOnHandQuantity
                );


        /*
         * ============================================================
         * 10. INVENTORY OUTBOX
         * ============================================================
         *
         * RabbitMQ'ya burada publish edilmiyor.
         *
         * Aynı DB transaction içerisinde outbox kaydı oluşturuluyor.
         */

        inventoryOutboxService
                .appendDecreaseCompletedEvent(
                        completedEvent
                );


        /*
         * ============================================================
         * 11. COMMIT
         * ============================================================
         *
         * Tek transaction:
         *
         * processed_events INSERT
         * stock_balances UPDATE
         * stock_movements INSERT
         * outbox_events INSERT
         *
         * birlikte COMMIT olur.
         */

        log.info(
                "Sale stock decrease prepared for commit. " +
                        "sourceEventId={}, saleId={}, stockItemId={}, " +
                        "quantitySold={}, previousOnHandQuantity={}, " +
                        "newOnHandQuantity={}, stockMovementId={}, " +
                        "completionEventId={}",
                event.eventId(),
                event.saleId(),
                event.stockItemId(),
                event.quantity(),
                previousOnHandQuantity,
                newOnHandQuantity,
                savedMovement.getId(),
                completedEvent.eventId()
        );
    }


    private void validateEvent(
            SaleCreatedEvent event
    ) {

        Objects.requireNonNull(
                event,
                "Sale created event cannot be null"
        );

        Objects.requireNonNull(
                event.eventId(),
                "Event ID cannot be null"
        );

        Objects.requireNonNull(
                event.saleId(),
                "Sale ID cannot be null"
        );

        Objects.requireNonNull(
                event.sellerId(),
                "Seller ID cannot be null"
        );

        Objects.requireNonNull(
                event.stockItemId(),
                "Stock item ID cannot be null"
        );

        Objects.requireNonNull(
                event.quantity(),
                "Sale quantity cannot be null"
        );

        Objects.requireNonNull(
                event.soldAt(),
                "Sold at cannot be null"
        );

        Objects.requireNonNull(
                event.occurredAt(),
                "Event occurrence time cannot be null"
        );


        if (!SUPPORTED_EVENT_TYPE.equals(
                event.eventType()
        )) {

            throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.eventType()
            );
        }


        if (event.eventVersion()
                != SUPPORTED_EVENT_VERSION) {

            throw new IllegalArgumentException(
                    "Unsupported event version: "
                            + event.eventVersion()
            );
        }


        if (event.saleId() <= 0) {
            throw new IllegalArgumentException(
                    "Sale ID must be greater than zero"
            );
        }




        if (event.quantity()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Sale quantity must be greater than zero"
            );
        }
    }
}