package com.muhammet.sales_service.sale.messaging.listener;

import com.muhammet.sales_service.sale.messaging.event.StockDecreaseFailedEvent;
import com.muhammet.sales_service.sale.service.SaleStockResultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDecreaseFailedEventListener {

    private final SaleStockResultService
            saleStockResultService;


    @RabbitListener(
            queues =
                    "${app.messaging.stock-result.failed-queue}",
            messageConverter =
                    "jsonMessageConverter"
    )
    public void handle(
            StockDecreaseFailedEvent event
    ) {

        log.warn(
                "StockDecreaseFailedEvent received. " +
                        "eventId={}, saleId={}, stockItemId={}, " +
                        "errorCode={}",
                event.eventId(),
                event.saleId(),
                event.stockItemId(),
                event.errorCode()
        );


        saleStockResultService
                .failStockUpdate(
                        event
                );
    }
}