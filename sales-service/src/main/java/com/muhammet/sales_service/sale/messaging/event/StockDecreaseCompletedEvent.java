package com.muhammet.sales_service.sale.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockDecreaseCompletedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long saleId,

        UUID stockItemId,

        UUID stockMovementId,

        BigDecimal quantityDecreased,

        BigDecimal newOnHandQuantity,

        Instant occurredAt

) {
}