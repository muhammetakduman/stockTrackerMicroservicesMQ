package com.muhammet.inventory_service.stock.messaging.recovery;

import com.muhammet.inventory_service.outbox.service.InventoryOutboxService;
import com.muhammet.inventory_service.stock.exception.StockProcessingException;
import com.muhammet.inventory_service.stock.messaging.event.SaleCreatedEvent;
import com.muhammet.inventory_service.stock.messaging.event.StockDecreaseFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaleCreatedMessageRecoverer
        implements MessageRecoverer {

    private static final int MAX_REASON_LENGTH = 500;

    private final JsonMapper jsonMapper;

    private final InventoryOutboxService inventoryOutboxService;


    @Override
    public void recover(
            Message message,
            Throwable cause
    ) {

        try {

            SaleCreatedEvent originalEvent =
                    jsonMapper.readValue(
                            message.getBody(),
                            SaleCreatedEvent.class
                    );


            StockProcessingException stockException =
                    findStockProcessingException(
                            cause
                    );


            String errorCode =
                    stockException != null
                            ? stockException.getErrorCode()
                            : "STOCK_DECREASE_FAILED";


            String failureReason =
                    resolveFailureReason(
                            stockException != null
                                    ? stockException
                                    : findRootCause(cause)
                    );


            StockDecreaseFailedEvent failedEvent =
                    StockDecreaseFailedEvent.create(
                            originalEvent.eventId(),
                            originalEvent.saleId(),
                            originalEvent.stockItemId(),
                            errorCode,
                            failureReason
                    );


            inventoryOutboxService
                    .appendDecreaseFailedEvent(
                            failedEvent
                    );


            log.error(
                    "SaleCreatedEvent processing exhausted retries. " +
                            "Failure event created. " +
                            "sourceEventId={}, saleId={}, " +
                            "stockItemId={}, errorCode={}",
                    originalEvent.eventId(),
                    originalEvent.saleId(),
                    originalEvent.stockItemId(),
                    errorCode,
                    cause
            );


        } catch (Exception recoveryException) {

            log.error(
                    "Failed to recover SaleCreatedEvent. " +
                            "Original message will be rejected to DLQ.",
                    recoveryException
            );
        }


        throw new AmqpRejectAndDontRequeueException(
                "SaleCreatedEvent retries exhausted",
                true,
                cause
        );
    }


    private StockProcessingException
    findStockProcessingException(
            Throwable throwable
    ) {

        Throwable current = throwable;

        while (current != null) {

            if (current
                    instanceof StockProcessingException exception) {

                return exception;
            }

            if (current.getCause() == current) {
                break;
            }

            current = current.getCause();
        }

        return null;
    }


    private Throwable findRootCause(
            Throwable throwable
    ) {

        Throwable current = throwable;

        while (current != null &&
                current.getCause() != null &&
                current.getCause() != current) {

            current = current.getCause();
        }

        return current != null
                ? current
                : throwable;
    }


    private String resolveFailureReason(
            Throwable throwable
    ) {

        String reason =
                throwable != null
                        ? throwable.getMessage()
                        : null;


        if (reason == null ||
                reason.isBlank()) {

            reason =
                    throwable != null
                            ? throwable
                            .getClass()
                            .getSimpleName()
                            : "Unknown stock decrease failure";
        }


        if (reason.length() >
                MAX_REASON_LENGTH) {

            return reason.substring(
                    0,
                    MAX_REASON_LENGTH
            );
        }

        return reason;
    }
}