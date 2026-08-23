package com.muhammet.inventory_service.stock.service;

import com.muhammet.inventory_service.inbox.service.ProcessedEventService;
import com.muhammet.inventory_service.outbox.service.InventoryOutboxService;
import com.muhammet.inventory_service.stock.entity.StockBalance;
import com.muhammet.inventory_service.stock.entity.StockItem;
import com.muhammet.inventory_service.stock.entity.StockMovement;
import com.muhammet.inventory_service.stock.exception.StockProcessingException;
import com.muhammet.inventory_service.stock.messaging.event.PurchaseCreatedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseCompletedEvent;
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
public class PurchaseStockService {
    private final ProcessedEventService processedEventService;
    private static final String PURCHASE_CREATED_CONSUMER = "inventory.purchase-created";

    private final InventoryOutboxService inventoryOutboxService;

    private static final String SUPPORTED_EVENT_TYPE =
            "purchase.created";

    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public void processPurchaseReceipt(
            PurchaseCreatedEvent event
    ) {

        /*
         * ============================================================
         * 1. EVENT VALIDATION
         * ============================================================
         *
         * Inbox'a herhangi bir kayıt atmadan önce gelen mesajın
         * gerçekten beklediğimiz event olduğundan emin oluyoruz.
         */
        validateEvent(event);


        /*
         * ============================================================
         * 2. INBOX / IDEMPOTENCY
         * ============================================================
         *
         * processed_events tablosuna:
         *
         * eventId + consumerName
         *
         * kombinasyonunu INSERT etmeye çalışır.
         *
         * İlk kez geliyorsa:
         *
         * INSERT başarılı
         * insertedRows = 1
         * firstProcessing = true
         *
         * Aynı event tekrar geldiyse:
         *
         * ON CONFLICT DO NOTHING
         * insertedRows = 0
         * firstProcessing = false
         */
        boolean firstProcessing =
                processedEventService.tryRegister(
                        event.eventId(),
                        event.eventType(),
                        PURCHASE_CREATED_CONSUMER
                );


        /*
         * Duplicate event ise artık aşağıdaki hiçbir
         * business işlemini yapmıyoruz.
         *
         * Böylece:
         *
         * StockBalance ikinci kez artmaz.
         * StockMovement ikinci kez oluşmaz.
         * Completion eventi ikinci kez yaratılmaz.
         */
        if (!firstProcessing) {

            log.info(
                    "Duplicate PurchaseCreatedEvent ignored by inbox. " +
                            "eventId={}, purchaseId={}, stockItemId={}",
                    event.eventId(),
                    event.purchaseId(),
                    event.stockItemId()
            );

            return;
        }


        /*
         * ============================================================
         * 3. STOCK BALANCE BUL
         * ============================================================
         *
         * Purchase eventindeki stockItemId için mevcut
         * balance kaydını buluyoruz.
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
         * 4. ESKİ STOK MİKTARI
         * ============================================================
         *
         * Movement kaydında:
         *
         * previous quantity
         * new quantity
         *
         * tutabilmek için artırmadan önce mevcut değeri alıyoruz.
         */
        BigDecimal previousOnHandQuantity =
                stockBalance.getOnHandQuantity();


        /*
         * ============================================================
         * 5. STOK ARTIRMA
         * ============================================================
         *
         * Purchase:
         *
         * "Tedarikçiden ürün satın aldım"
         *
         * anlamına geldiğinden inventory stoğu artırılır.
         *
         * Örneğin:
         *
         * mevcut = 500
         * purchase = 100
         *
         * yeni = 600
         */

        stockBalance.increaseOnHandQuantity(
                event.quantity()
        );


        /*
         * Entity managed olduğu için transaction sonunda Hibernate
         * dirty checking ile UPDATE yapabilir.
         *
         * Ancak repository.save(...) kullanmak da burada sorun değildir.
         *
         * Açıklığı korumak adına save ediyoruz.
         */
        stockBalanceRepository.save(stockBalance);


        BigDecimal newOnHandQuantity =
                stockBalance.getOnHandQuantity();


        /*
         * ============================================================
         * 6. STOCK MOVEMENT
         * ============================================================
         *
         * Balance bize yalnızca:
         *
         * "şu anda kaç stok var?"
         *
         * cevabını verir.
         *
         * Movement ise:
         *
         * "stok neden değişti?"
         *
         * sorusunun cevabıdır.
         *
         * Örneğin:
         *
         * PURCHASE_INCREASE
         * +100
         *
         * previous = 500
         * new      = 600
         *
         * sourceEventId = purchase.created eventId
         */
        StockItem stockItem =
                stockBalance.getStockItem();

        StockMovement movement =
                StockMovement.purchaseReceipt(
                        event.eventId(),
                        stockItem,
                        event.quantity(),
                        previousOnHandQuantity,
                        newOnHandQuantity,
                        event.purchaseId(),
                        event.occurredAt()
                );

        StockMovement savedMovement =
                stockMovementRepository.save(movement);
        /*
         * ============================================================
         * 7. COMPLETION EVENT
         * ============================================================
         *
         * Inventory işlemi başarıyla hazırlandı.
         *
         * Purchase-service'e:
         *
         * "İstediğin stok artırma işlemini yaptım."
         *
         * cevabını gönderecek event oluşturulur.
         */
        StockIncreaseCompletedEvent completedEvent =
                StockIncreaseCompletedEvent.create(
                        event.eventId(),
                        event.purchaseId(),
                        event.stockItemId(),
                        savedMovement.getId(),
                        event.quantity(),
                        newOnHandQuantity
                );


        /*
         * ============================================================
         * 8. INVENTORY OUTBOX
         * ============================================================
         *
         * BURADA RABBITMQ'YA DIRECT PUBLISH YOK.
         *
         * Completion eventi outbox_events tablosuna PENDING
         * olarak yazılır.
         *
         * InventoryOutboxService.appendCompletedEvent()
         * Propagation.MANDATORY olduğu için bizim mevcut
         * transaction'ımıza katılır.
         */
        inventoryOutboxService.appendCompletedEvent(
                completedEvent
        );


        /*
         * ============================================================
         * 9. TRANSACTION
         * ============================================================
         *
         * Metot başarılı şekilde biterse transaction içerisinde:
         *
         * processed_events INSERT
         * stock_balances UPDATE
         * stock_movements INSERT
         * outbox_events INSERT
         *
         * birlikte COMMIT olur.
         */
        log.info(
                "Purchase receipt prepared for commit. " +
                        "sourceEventId={}, purchaseId={}, stockItemId={}, " +
                        "quantityAdded={}, previousOnHandQuantity={}, " +
                        "newOnHandQuantity={}, stockMovementId={}, " +
                        "completionEventId={}",
                event.eventId(),
                event.purchaseId(),
                event.stockItemId(),
                event.quantity(),
                previousOnHandQuantity,
                newOnHandQuantity,
                savedMovement.getId(),
                completedEvent.eventId()
        );
    }


    private void validateEvent(
            PurchaseCreatedEvent event
    ) {
        Objects.requireNonNull(
                event,
                "Purchase created event cannot be null"
        );

        Objects.requireNonNull(
                event.eventId(),
                "Event ID cannot be null"
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
                event.quantity(),
                "Purchase quantity cannot be null"
        );

        Objects.requireNonNull(
                event.occurredAt(),
                "Event occurrence time cannot be null"
        );

        if (!SUPPORTED_EVENT_TYPE.equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unsupported event type: "
                            + event.eventType()
            );
        }

        if (event.eventVersion() != SUPPORTED_EVENT_VERSION) {
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

        if (event.quantity()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Purchase quantity must be greater than zero"
            );
        }
    }
}