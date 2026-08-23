package com.muhammet.sales_service.sale.messaging.listener;

import com.muhammet.sales_service.sale.messaging.event.StockDecreaseCompletedEvent;
import com.muhammet.sales_service.sale.service.SaleStockResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDecreaseCompletedEventListener {

    private final SaleStockResultService
            saleStockResultService;


    @RabbitListener(
            queues =
                    "${app.messaging.stock-result.completed-queue}",
            messageConverter =
                    "jsonMessageConverter"
    )
    public void handle(
            StockDecreaseCompletedEvent event
    ) {

        log.info(
                "StockDecreaseCompletedEvent received. " +
                        "eventId={}, saleId={}, stockItemId={}",
                event.eventId(),
                event.saleId(),
                event.stockItemId()
        );


        saleStockResultService
                .completeStockUpdate(
                        event
                );
    }
}