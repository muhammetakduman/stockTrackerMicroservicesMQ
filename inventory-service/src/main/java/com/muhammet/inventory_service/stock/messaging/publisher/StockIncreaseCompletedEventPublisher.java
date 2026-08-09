package com.muhammet.inventory_service.stock.messaging.publisher;

import com.muhammet.inventory_service.stock.messaging.event.StockIncreaseCompletedEvent;

public interface StockIncreaseCompletedEventPublisher {

    void publish(StockIncreaseCompletedEvent event);
}