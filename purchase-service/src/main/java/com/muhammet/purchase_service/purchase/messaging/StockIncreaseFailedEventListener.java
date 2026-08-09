package com.muhammet.purchase_service.purchase.messaging;

import com.muhammet.purchase_service.purchase.messaging.event.StockIncreaseFailedEvent;
import com.muhammet.purchase_service.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockIncreaseFailedEventListener {

    private final PurchaseService purchaseService;

    @RabbitListener(
            queues =
                    "${app.messaging.stock-result.failed-queue}"
    )
    public void handle(
            StockIncreaseFailedEvent event
    ) {
        log.warn(
                "StockIncreaseFailedEvent received. " +
                        "eventId={}, sourceEventId={}, " +
                        "purchaseId={}, stockItemId={}, " +
                        "errorCode={}",
                event.eventId(),
                event.sourceEventId(),
                event.purchaseId(),
                event.stockItemId(),
                event.errorCode()
        );

        purchaseService.failStockUpdate(event);

        log.info(
                "StockIncreaseFailedEvent processed. " +
                        "eventId={}, purchaseId={}",
                event.eventId(),
                event.purchaseId()
        );
    }
}