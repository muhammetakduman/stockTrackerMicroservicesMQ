package com.muhammet.purchase_service.purchase.messaging.event;


import java.time.Instant;
import java.util.UUID;

public record StockIncreaseFailedEvent(

        UUID eventId,

        String eventType,

        int eventVersion,

        UUID sourceEventId,

        Long purchaseId,

        UUID stockItemId,

        String errorCode,

        String failureReason,

        Instant occurredAt

) {
}