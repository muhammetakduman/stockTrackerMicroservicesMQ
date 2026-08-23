package com.muhammet.sales_service.config;

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
@ConfigurationProperties(
        prefix = "app.messaging.stock-result"
)
public class StockResultMessagingProperties {

    @NotBlank
    private String exchange;

    @NotBlank
    private String completedRoutingKey;

    @NotBlank
    private String failedRoutingKey;

    @NotBlank
    private String completedQueue;

    @NotBlank
    private String failedQueue;

    @NotBlank
    private String deadLetterExchange;

    @NotBlank
    private String completedDlq;

    @NotBlank
    private String failedDlq;

    @NotBlank
    private String completedDeadLetterRoutingKey;

    @NotBlank
    private String failedDeadLetterRoutingKey;
}