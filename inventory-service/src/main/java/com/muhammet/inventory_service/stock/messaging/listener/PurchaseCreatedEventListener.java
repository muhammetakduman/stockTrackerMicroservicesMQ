package com.muhammet.inventory_service.stock.messaging.listener;

import com.muhammet.inventory_service.stock.messaging.event.PurchaseCreatedEvent;
import com.muhammet.inventory_service.stock.service.PurchaseStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseCreatedEventListener {

    private final PurchaseStockService purchaseStockService;

    @RabbitListener(
            queues = "${app.messaging.purchase.queue}"
    )
    public void handle(PurchaseCreatedEvent event) {

        log.info(
                "PurchaseCreatedEvent received. " +
                        "eventId={}, purchaseId={}, stockItemId={}",
                event.eventId(),
                event.purchaseId(),
                event.stockItemId()
        );

        purchaseStockService.processPurchaseReceipt(event);

        /*
         * Service metodu döndüğünde transaction commit edilmiş olur.
         */
        log.info(
                "PurchaseCreatedEvent processed successfully. " +
                        "eventId={}, purchaseId={}",
                event.eventId(),
                event.purchaseId()
        );
    }
}