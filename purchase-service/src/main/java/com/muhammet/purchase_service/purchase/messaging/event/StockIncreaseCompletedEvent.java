package com.muhammet.purchase_service.purchase.messaging.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockIncreaseCompletedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long purchaseId,

        UUID stockItemId,

        UUID stockMovementId,

        BigDecimal quantityAdded,

        BigDecimal newOnHandQuantity,

        Instant occurredAt

) {
}