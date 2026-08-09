package com.muhammet.purchase_service.purchase.messaging.publisher;

import com.muhammet.purchase_service.purchase.messaging.event.PurchaseCreatedEvent;

public interface PurchaseEventPublisher {
    void publish(PurchaseCreatedEvent event);
}
