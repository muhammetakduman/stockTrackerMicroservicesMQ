package com.muhammet.sales_service.sale.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record StockDecreaseFailedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long saleId,

        UUID stockItemId,

        String errorCode,

        String failureReason,

        Instant occurredAt

) {
}