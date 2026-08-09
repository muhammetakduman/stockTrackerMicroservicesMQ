package com.muhammet.inventory_service.stock.messaging.publisher;

import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseFailedEvent;

public interface StockIncreaseFailedEventPublisher {
    void publish(StockIncreaseFailedEvent event);
}
