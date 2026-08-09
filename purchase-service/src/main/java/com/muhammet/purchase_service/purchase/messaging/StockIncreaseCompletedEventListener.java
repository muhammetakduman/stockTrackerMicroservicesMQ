package com.muhammet.purchase_service.purchase.messaging;

import com.muhammet.purchase_service.purchase.messaging.event.StockIncreaseCompletedEvent;
import com.muhammet.purchase_service.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockIncreaseCompletedEventListener {

    private final PurchaseService purchaseService;

    @RabbitListener(
            queues = "${app.messaging.stock-result.queue}"
    )
    public void handle(
            StockIncreaseCompletedEvent event
    ) {
        log.info(
                "StockIncreaseCompletedEvent received. " +
                        "eventId={}, sourceEventId={}, purchaseId={}",
                event.eventId(),
                event.sourceEventId(),
                event.purchaseId()
        );

        purchaseService.completeStockUpdate(event);

        log.info(
                "StockIncreaseCompletedEvent processed successfully. " +
                        "eventId={}, purchaseId={}",
                event.eventId(),
                event.purchaseId()
        );
    }
}