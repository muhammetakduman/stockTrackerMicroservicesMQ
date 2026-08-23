package com.muhammet.inventory_service.outbox.service;

import com.muhammet.inventory_service.config.StockResultMessagingProperties;
import com.muhammet.inventory_service.outbox.entity.OutboxEvent;
import com.muhammet.inventory_service.outbox.exception.OutboxSerializationException;
import com.muhammet.inventory_service.outbox.repository.OutboxEventRepository;
import com.muhammet.inventory_service.stock.messaging.event.StockDecreaseCompletedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockDecreaseFailedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseCompletedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseFailedEvent;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryOutboxService {

    private static final String AGGREGATE_TYPE =
            "STOCK_ITEM";

    private final OutboxEventRepository
            outboxEventRepository;

    private final StockResultMessagingProperties
            messagingProperties;

    private final JsonMapper
            jsonMapper;


    // =========================================================
    // PURCHASE SUCCESS
    // =========================================================

    /*
     * Purchase işlemi başarıyla stoğu artırdıktan sonra:
     *
     * stock.increase.completed
     *
     * eventini outbox'a yazar.
     *
     * Bu metodun mevcut business transaction içerisinde
     * çalışması zorunludur.
     */
    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public OutboxEvent appendCompletedEvent(
            StockIncreaseCompletedEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Stock increase completed event cannot be null"
            );
        }

        ensureEventDoesNotExist(
                event.eventId()
        );

        String payload =
                serializeCompletedEvent(
                        event
                );

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),

                        /*
                         * stock.increase.completed
                         */
                        messagingProperties.getRoutingKey(),

                        payload
                );

        return outboxEventRepository.save(
                outboxEvent
        );
    }


    // =========================================================
    // PURCHASE FAILED
    // =========================================================

    /*
     * Purchase stock işlemi retry'lar sonrasında
     * başarısız olduğunda:
     *
     * stock.increase.failed
     *
     * eventini outbox'a yazar.
     *
     * Orijinal transaction rollback olduğu için
     * burada bağımsız transaction gerekir.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public OutboxEvent appendFailedEvent(
            StockIncreaseFailedEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Stock increase failed event cannot be null"
            );
        }

        ensureEventDoesNotExist(
                event.eventId()
        );

        String payload =
                serializeFailedEvent(
                        event
                );

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),

                        /*
                         * stock.increase.failed
                         */
                        messagingProperties.getFailedRoutingKey(),

                        payload
                );

        return outboxEventRepository.save(
                outboxEvent
        );
    }


    // =========================================================
    // SALE SUCCESS
    // =========================================================

    /*
     * Sale işlemi başarıyla stoğu azalttığında:
     *
     * stock.decrease.completed
     *
     * eventini outbox'a yazar.
     *
     * SaleStockService transaction'ının parçasıdır.
     */
    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public OutboxEvent appendDecreaseCompletedEvent(
            StockDecreaseCompletedEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Stock decrease completed event cannot be null"
            );
        }

        ensureEventDoesNotExist(
                event.eventId()
        );

        String payload =
                serializeDecreaseCompletedEvent(
                        event
                );

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),

                        /*
                         * stock.decrease.completed
                         */
                        messagingProperties.getDecreaseRoutingKey(),

                        payload
                );

        return outboxEventRepository.save(
                outboxEvent
        );
    }


    // =========================================================
    // SALE FAILED
    // =========================================================

    /*
     * Sale stock işlemi retry'lar sonrasında
     * başarısız olduğunda:
     *
     * stock.decrease.failed
     *
     * eventini outbox'a yazar.
     *
     * Asıl SaleStockService transaction'ı rollback
     * olduğu için bağımsız transaction açılır.
     */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public OutboxEvent appendDecreaseFailedEvent(
            StockDecreaseFailedEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Stock decrease failed event cannot be null"
            );
        }

        ensureEventDoesNotExist(
                event.eventId()
        );

        String payload =
                serializeDecreaseFailedEvent(
                        event
                );

        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        AGGREGATE_TYPE,
                        event.stockItemId().toString(),
                        event.eventType(),
                        event.eventVersion(),
                        messagingProperties.getExchange(),

                        /*
                         * stock.decrease.failed
                         */
                        messagingProperties
                                .getDecreaseFailedRoutingKey(),

                        payload
                );

        return outboxEventRepository.save(
                outboxEvent
        );
    }


    // =========================================================
    // SERIALIZATION
    // =========================================================

    private String serializeCompletedEvent(
            StockIncreaseCompletedEvent event
    ) {

        try {

            return jsonMapper.writeValueAsString(
                    event
            );

        } catch (JacksonException exception) {

            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }


    private String serializeFailedEvent(
            StockIncreaseFailedEvent event
    ) {

        try {

            return jsonMapper.writeValueAsString(
                    event
            );

        } catch (JacksonException exception) {

            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }


    private String serializeDecreaseCompletedEvent(
            StockDecreaseCompletedEvent event
    ) {

        try {

            return jsonMapper.writeValueAsString(
                    event
            );

        } catch (JacksonException exception) {

            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }


    private String serializeDecreaseFailedEvent(
            StockDecreaseFailedEvent event
    ) {

        try {

            return jsonMapper.writeValueAsString(
                    event
            );

        } catch (JacksonException exception) {

            throw new OutboxSerializationException(
                    event.eventId(),
                    exception
            );
        }
    }


    // =========================================================
    // DUPLICATE PROTECTION
    // =========================================================

    /*
     * Aynı business event ID'si ile ikinci bir
     * outbox kaydı oluşmasını engeller.
     */
    private void ensureEventDoesNotExist(
            UUID eventId
    ) {

        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID cannot be null"
            );
        }

        if (outboxEventRepository.existsByEventId(
                eventId
        )) {

            throw new IllegalArgumentException(
                    "Outbox event already exists. eventId="
                            + eventId
            );
        }
    }
}