package com.muhammet.inventory_service.stock.messaging.listener;

import com.muhammet.inventory_service.stock.messaging.event.SaleCreatedEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SaleCreatedEventListener {


    @RabbitListener(
            queues = "${app.messaging.sale.queue}",
            messageConverter = "jsonMessageConverter"
    )
    public void handle(
            SaleCreatedEvent event
    ) {

        log.info(
                "SaleCreatedEvent received. " +
                        "eventId={}, eventType={}, version={}, " +
                        "saleId={}, sellerId={}, stockItemId={}, " +
                        "quantity={}, soldAt={}, occurredAt={}",
                event.eventId(),
                event.eventType(),
                event.eventVersion(),
                event.saleId(),
                event.sellerId(),
                event.stockItemId(),
                event.quantity(),
                event.soldAt(),
                event.occurredAt()
        );
    }
}