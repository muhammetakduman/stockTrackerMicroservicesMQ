package com.muhammet.sales_service.sale.messaging.listener;

import com.muhammet.sales_service.sale.messaging.event.StockDecreaseFailedEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockDecreaseFailedEventListener {


    @RabbitListener(
            queues = "${app.messaging.stock-result.failed-queue}",
            messageConverter = "jsonMessageConverter"
    )
    public void handle(
            StockDecreaseFailedEvent event
    ) {

        log.warn(
                "StockDecreaseFailedEvent received. " +
                        "eventId={}, sourceEventId={}, saleId={}, " +
                        "stockItemId={}, errorCode={}, failureReason={}",
                event.eventId(),
                event.sourceEventId(),
                event.saleId(),
                event.stockItemId(),
                event.errorCode(),
                event.failureReason()
        );
    }
}