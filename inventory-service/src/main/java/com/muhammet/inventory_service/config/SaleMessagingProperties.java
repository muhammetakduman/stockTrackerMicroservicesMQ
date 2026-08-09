package com.muhammet.inventory_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.messaging.sale")
public class SaleMessagingProperties {

    private String exchange;

    private String routingKey;

    private String queue;

    private String deadLetterExchange;

    private String deadLetterQueue;

    private String deadLetterRoutingKey;
}