package com.muhammet.inventory_service.config;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.messaging.purchase")
public class PurchaseMessagingProperties {

    @NotBlank
    private String exchange;

    @NotBlank
    private String queue;

    @NotBlank
    private String routingKey;

    @NotBlank
    private String deadLetterExchange;

    @NotBlank
    private String deadLetterQueue;

    @NotBlank
    private String deadLetterRoutingKey;
}