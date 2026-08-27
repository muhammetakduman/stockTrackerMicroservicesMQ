package com.muhammet.purchase_service.purchase.service;

import com.muhammet.purchase_service.outbox.service.PurchaseOutboxService;
import com.muhammet.purchase_service.purchase.domain.Purchase;
import com.muhammet.purchase_service.purchase.domain.PurchaseStatus;
import com.muhammet.purchase_service.purchase.dto.request.CreatePurchaseRequest;
import com.muhammet.purchase_service.purchase.dto.response.PageResponse;
import com.muhammet.purchase_service.purchase.dto.response.PurchaseResponse;
import com.muhammet.purchase_service.purchase.exception.PurchaseNotFoundException;
import com.muhammet.purchase_service.purchase.messaging.event.PurchaseCreatedEvent;
import com.muhammet.purchase_service.purchase.messaging.event.StockIncreaseCompletedEvent;
import com.muhammet.purchase_service.purchase.messaging.event.StockIncreaseFailedEvent;
import com.muhammet.purchase_service.purchase.repository.PurchaseRepository;
import com.muhammet.purchase_service.inbox.service.ProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.muhammet.purchase_service.purchase.specification.PurchaseSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseService {

    private static final String STOCK_COMPLETED_EVENT_TYPE =
            "stock.increase.completed";

    private static final int STOCK_COMPLETED_EVENT_VERSION = 1;

    private static final String STOCK_FAILED_EVENT_TYPE =
            "stock.increase.failed";

    private static final int STOCK_FAILED_EVENT_VERSION = 1;

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private static final String STOCK_COMPLETED_CONSUMER =
            "purchase.stock-increase-completed";

    private static final String STOCK_FAILED_CONSUMER =
            "purchase.stock-increase-failed";

    private final PurchaseRepository purchaseRepository;

    /*
     * PurchaseCreatedEvent artık doğrudan RabbitMQ'ya gönderilmiyor.
     * Aynı transaction içerisinde outbox_events tablosuna yazılıyor.
     */
    private final PurchaseOutboxService purchaseOutboxService;
    private final ProcessedEventService processedEventService;

    @Transactional
    public PurchaseResponse createPurchase(
            CreatePurchaseRequest request
    ) {
        Objects.requireNonNull(
                request,
                "Create purchase request cannot be null"
        );

        Purchase purchase = Purchase.create(
                request.stockItemId(),
                request.quantity(),
                request.unitPrice(),
                request.supplierName(),
                request.purchasedAt()
        );

        /*
         * Purchase ID'sinin oluşması için kayıt veritabanına
         * gönderiliyor. Transaction henüz commit edilmiş değildir.
         */
        Purchase savedPurchase =
                purchaseRepository.saveAndFlush(purchase);

        PurchaseCreatedEvent event =
                PurchaseCreatedEvent.from(savedPurchase);

        /*
         * Event RabbitMQ'ya doğrudan gönderilmez.
         * outbox_events tablosuna PENDING olarak kaydedilir.
         *
         * Purchase ve OutboxEvent aynı transaction içerisindedir.
         */
        purchaseOutboxService.append(event);

        log.info(
                "Purchase and outbox event created. " +
                        "purchaseId={}, eventId={}, " +
                        "stockItemId={}, status={}",
                savedPurchase.getId(),
                event.eventId(),
                savedPurchase.getStockItemId(),
                savedPurchase.getStatus()
        );

        return PurchaseResponse.from(savedPurchase);
    }

    @Transactional
    public void failStockUpdate(
            StockIncreaseFailedEvent event
    ) {
        validateStockIncreaseFailedEvent(event);

        boolean firstProcessing = processedEventService.tryRegister(
                event.eventId(),
                event.eventType(),
                STOCK_FAILED_CONSUMER
        );

        if(!firstProcessing){
            log.info("Duplicate StockIncreaseFailedEvent ignored by inbox. " +
                    "eventId={}, purchaseId={}",
                    event.eventId(), event.purchaseId());
            return;
        }
        Purchase purchase = purchaseRepository.findById(event.purchaseId())
                .orElseThrow(()-> new PurchaseNotFoundException(event.purchaseId()));

        if(purchase.getStatus() == PurchaseStatus.FAILED){
            log.info("Stock failure event ignored because purchae " +
                    "is already failed. " +
                    "eventId={}, purchaseId={}, errorCode={}",
                    event.eventId(), event.purchaseId(), event.errorCode());
            return;
        }
        if(purchase.getStatus() == PurchaseStatus.COMPLETED){
            log.warn(
                    "Stock failure event ignored because purchase " +
                            "is already completed. " +
                            "eventId={}, purchaseId={}, errorCode={}",
                    event.eventId(), event.purchaseId(), event.errorCode()
            );
            return;
        }
        if (!purchase.getStockItemId().equals(event.stockItemId())){
            throw  new IllegalArgumentException(
                    "Stock item mismatch for purchase " +
                            purchase.getId()
                    +". Expected: "
                    + purchase.getStockItemId()
                    + ", received: "
                    + event.stockItemId()
            );
        }
        String storedFailureReason = buildFailureReason(event);
        purchase.markStockUpdateFailed(storedFailureReason);
        log.error(
                "Purchase stock update failed. " +
                        "eventId={}, sourceEventId={}, purchaseId={}, " +
                        "stockItemId={}, errorCode={}, failureReason={}",
                event.eventId(),
                event.sourceEventId(),
                purchase.getId(),
                event.stockItemId(),
                event.errorCode(),
                event.failureReason()

        );
    }

    @Transactional
    public void completeStockUpdate(
            StockIncreaseCompletedEvent event
    ) {
        /*
         * Önce event yapısının beklediğimiz formata
         * uygun olduğunu doğruluyoruz.
         */
        validateStockIncreaseCompletedEvent(event);

        /*
         * INBOX
         *
         * Aynı event bu consumer tarafından daha önce
         * işlendi mi?
         */
        boolean firstProcessing =
                processedEventService.tryRegister(
                        event.eventId(),
                        event.eventType(),
                        STOCK_COMPLETED_CONSUMER
                );

        /*
         * Aynı eventId ikinci kez geldiyse business
         * işlemini tekrar yapmıyoruz.
         */
        if (!firstProcessing) {

            log.info(
                    "Duplicate StockIncreaseCompletedEvent ignored by inbox. " +
                            "eventId={}, purchaseId={}",
                    event.eventId(),
                    event.purchaseId()
            );

            return;
        }

        Purchase purchase = purchaseRepository
                .findById(event.purchaseId())
                .orElseThrow(() ->
                        new PurchaseNotFoundException(
                                event.purchaseId()
                        )
                );

        /*
         * Business idempotency.
         *
         * Farklı eventId'li başka bir completion eventi
         * yanlışlıkla aynı purchase için gelirse Inbox
         * bunu duplicate saymaz.
         *
         * Purchase state burada ikinci koruma katmanıdır.
         */
        if (purchase.getStatus() ==
                PurchaseStatus.COMPLETED) {

            log.info(
                    "Stock completion event ignored because purchase " +
                            "is already completed. " +
                            "eventId={}, purchaseId={}",
                    event.eventId(),
                    event.purchaseId()
            );

            return;
        }

        /*
         * FAILED olmuş purchase artık başarıya çevrilemez.
         */
        if (purchase.getStatus() ==
                PurchaseStatus.FAILED) {

            log.warn(
                    "Stock completion event ignored because purchase " +
                            "is already failed. eventId={}, purchaseId={}",
                    event.eventId(),
                    event.purchaseId()
            );

            return;
        }

        /*
         * Purchase'ın istediği stok kalemi ile inventory
         * cevabındaki stok kalemi aynı mı?
         */
        if (!purchase.getStockItemId()
                .equals(event.stockItemId())) {

            throw new IllegalStateException(
                    "Stock item mismatch for purchase "
                            + purchase.getId()
                            + ". Expected: "
                            + purchase.getStockItemId()
                            + ", received: "
                            + event.stockItemId()
            );
        }

        /*
         * Inventory gerçekten purchase'ta istenen kadar
         * stok artırmış mı?
         */
        if (purchase.getQuantity()
                .compareTo(event.quantityAdded()) != 0) {

            throw new IllegalStateException(
                    "Quantity mismatch for purchase "
                            + purchase.getId()
                            + ". Expected: "
                            + purchase.getQuantity()
                            + ", received: "
                            + event.quantityAdded()
            );
        }

        purchase.markStockUpdateCompleted();

        log.info(
                "Purchase stock update completed. " +
                        "eventId={}, sourceEventId={}, purchaseId={}, " +
                        "stockItemId={}, stockMovementId={}, " +
                        "quantityAdded={}, newOnHandQuantity={}",
                event.eventId(),
                event.sourceEventId(),
                purchase.getId(),
                event.stockItemId(),
                event.stockMovementId(),
                event.quantityAdded(),
                event.newOnHandQuantity()
        );
    }

    @Transactional(readOnly = true)
    public PurchaseResponse getPurchaseById(
            Long purchaseId
    ) {
        if (purchaseId == null || purchaseId <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
            );
        }

        Purchase purchase = purchaseRepository
                .findById(purchaseId)
                .orElseThrow(() ->
                        new PurchaseNotFoundException(
                                purchaseId
                        )
                );

        return PurchaseResponse.from(purchase);
    }

    private void validateStockIncreaseFailedEvent(
            StockIncreaseFailedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Stock increase failed event cannot be null"
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
                event.purchaseId(),
                "Purchase ID cannot be null"
        );

        Objects.requireNonNull(
                event.stockItemId(),
                "Stock item ID cannot be null"
        );

        Objects.requireNonNull(
                event.occurredAt(),
                "Occurrence time cannot be null"
        );

        if (!STOCK_FAILED_EVENT_TYPE.equals(
                event.eventType()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.eventType()
            );
        }

        if (event.eventVersion() !=
                STOCK_FAILED_EVENT_VERSION) {

            throw new IllegalArgumentException(
                    "Unsupported event version: "
                            + event.eventVersion()
            );
        }

        if (event.purchaseId() <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
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

    private void validateStockIncreaseCompletedEvent(
            StockIncreaseCompletedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Stock increase completed event cannot be null"
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
                event.purchaseId(),
                "Purchase ID cannot be null"
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
                event.quantityAdded(),
                "Quantity added cannot be null"
        );

        Objects.requireNonNull(
                event.newOnHandQuantity(),
                "New on-hand quantity cannot be null"
        );

        Objects.requireNonNull(
                event.occurredAt(),
                "Occurrence time cannot be null"
        );

        if (!STOCK_COMPLETED_EVENT_TYPE.equals(
                event.eventType()
        )) {
            throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.eventType()
            );
        }

        if (event.eventVersion() !=
                STOCK_COMPLETED_EVENT_VERSION) {

            throw new IllegalArgumentException(
                    "Unsupported event version: "
                            + event.eventVersion()
            );
        }

        if (event.purchaseId() <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than zero"
            );
        }

        if (event.quantityAdded()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Quantity added must be greater than zero"
            );
        }

        if (event.newOnHandQuantity()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "New on-hand quantity cannot be negative"
            );
        }
    }

    private String buildFailureReason(
            StockIncreaseFailedEvent event
    ) {
        String reason =
                event.errorCode()
                        + ": "
                        + event.failureReason();

        if (reason.length() >
                MAX_FAILURE_REASON_LENGTH) {

            return reason.substring(
                    0,
                    MAX_FAILURE_REASON_LENGTH
            );
        }

        return reason;
    }
    @Transactional(readOnly = true)
    public PageResponse<PurchaseResponse> findAll(
            PurchaseStatus status,
            UUID stockItemId,
            String supplierName,
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


        Specification<Purchase> specification =
                PurchaseSpecification
                        .hasStatus(status)
                        .and(
                                PurchaseSpecification
                                        .hasStockItemId(stockItemId)
                        )
                        .and(
                                PurchaseSpecification
                                        .hasSupplierName(supplierName)
                        )
                        .and(
                                PurchaseSpecification
                                        .purchasedFrom(from)
                        )
                        .and(
                                PurchaseSpecification
                                        .purchasedTo(to)
                        );


        PageRequest pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "purchasedAt"
                        ).and(
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "id"
                                )
                        )
                );


        Page<Purchase> result =
                purchaseRepository.findAll(
                        specification,
                        pageable
                );


        return new PageResponse<>(
                result.getContent()
                        .stream()
                        .map(PurchaseResponse::from)
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
                    "Page değeri negatif olamaz"
            );
        }

        if (size < 1 || size > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Size değeri 1 ile 100 arasında olmalıdır"
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
                    "From tarihi to tarihinden sonra olamaz"
            );
        }
    }
}