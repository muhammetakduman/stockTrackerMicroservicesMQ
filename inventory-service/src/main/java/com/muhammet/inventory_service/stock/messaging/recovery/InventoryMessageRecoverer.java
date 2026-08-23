package com.muhammet.inventory_service.stock.messaging.recovery;

import com.muhammet.inventory_service.config.PurchaseMessagingProperties;
import com.muhammet.inventory_service.config.SaleMessagingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryMessageRecoverer
        implements MessageRecoverer {

    private final PurchaseCreatedMessageRecovererHandler
            purchaseCreatedMessageRecovererHandler;

    private final SaleCreatedMessageRecovererHandler
            saleCreatedMessageRecovererHandler;

    private final PurchaseMessagingProperties
            purchaseProperties;

    private final SaleMessagingProperties
            saleProperties;


    @Override
    public void recover(
            Message message,
            Throwable cause
    ) {

        String consumerQueue =
                message
                        .getMessageProperties()
                        .getConsumerQueue();


        if (Objects.equals(
                purchaseProperties.getQueue(),
                consumerQueue
        )) {

            purchaseCreatedMessageRecovererHandler.recover(
                    message,
                    cause
            );

            return;
        }


        if (Objects.equals(
                saleProperties.getQueue(),
                consumerQueue
        )) {

            saleCreatedMessageRecovererHandler.recover(
                    message,
                    cause
            );

            return;
        }


        log.error(
                "No recovery strategy configured for RabbitMQ queue. " +
                        "consumerQueue={}",
                consumerQueue,
                cause
        );


        throw new AmqpRejectAndDontRequeueException(
                "No recovery strategy configured for queue: "
                        + consumerQueue,
                true,
                cause
        );
    }
}