package com.muhammet.sales_service.outbox.service;

import com.muhammet.sales_service.outbox.entity.OutboxEvent;
import com.muhammet.sales_service.outbox.repository.OutboxEventRepository;
import com.muhammet.sales_service.sale.messaging.event.SaleCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOutboxService {

    private static final String AGGREGATE_TYPE =
            "SALE";

    private final OutboxEventRepository
            outboxEventRepository;

    private final ObjectMapper
            objectMapper;


    @Value("${app.messaging.sale.exchange}")
    private String saleExchange;


    @Value("${app.messaging.sale.routing-key}")
    private String saleCreatedRoutingKey;


    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public void append(
            SaleCreatedEvent event
    ) {

        Objects.requireNonNull(
                event,
                "Sale created event cannot be null"
        );


        String payload;

        try {

            payload =
                    objectMapper.writeValueAsString(
                            event
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "SaleCreatedEvent could not be serialized",
                    exception
            );
        }


        OutboxEvent outboxEvent =
                OutboxEvent.pending(
                        event.eventId(),
                        event.eventType(),
                        AGGREGATE_TYPE,
                        event.saleId().toString(),
                        saleExchange,
                        saleCreatedRoutingKey,
                        payload
                );


        outboxEventRepository.save(
                outboxEvent
        );


        log.info(
                "SaleCreatedEvent appended to outbox. " +
                        "eventId={}, saleId={}, eventType={}",
                event.eventId(),
                event.saleId(),
                event.eventType()
        );
    }
}