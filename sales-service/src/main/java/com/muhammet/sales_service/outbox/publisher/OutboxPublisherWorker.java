package com.muhammet.sales_service.outbox.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherWorker {

    private final OutboxPublisherService outboxPublisherService;


    @Scheduled(
            fixedDelayString =
                    "${app.outbox.publisher.fixed-delay-ms:2000}"
    )
    public void publishPendingEvents() {

        try {

            outboxPublisherService.publishPendingBatch();

        } catch (Exception exception) {

            log.error(
                    "Unexpected error while publishing sales outbox events",
                    exception
            );
        }
    }
}