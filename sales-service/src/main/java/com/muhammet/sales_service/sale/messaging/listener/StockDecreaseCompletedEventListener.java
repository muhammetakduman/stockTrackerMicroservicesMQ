package com.muhammet.sales_service.sale.messaging.listener;

import com.muhammet.sales_service.sale.messaging.event.StockDecreaseCompletedEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockDecreaseCompletedEventListener {


    @RabbitListener(
            queues = "${app.messaging.stock-result.completed-queue}",
            messageConverter = "jsonMessageConverter"
    )
    public void handle(
            StockDecreaseCompletedEvent event
    ) {

        log.info(
                "StockDecreaseCompletedEvent received. " +
                        "eventId={}, sourceEventId={}, saleId={}, " +
                        "stockItemId={}, stockMovementId={}, " +
                        "quantityDecreased={}, newOnHandQuantity={}",
                event.eventId(),
                event.sourceEventId(),
                event.saleId(),
                event.stockItemId(),
                event.stockMovementId(),
                event.quantityDecreased(),
                event.newOnHandQuantity()
        );
    }
}