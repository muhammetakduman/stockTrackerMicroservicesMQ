package com.muhammet.purchase_service.config;

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

    // StockIncreaseCompletedEvent ana queue ayarları
    @NotBlank
    private String queue;

    @NotBlank
    private String routingKey;

    // StockIncreaseFailedEvent ana queue ayarları
    @NotBlank
    private String failedQueue;

    @NotBlank
    private String failedRoutingKey;

    // Ortak dead-letter exchange
    @NotBlank
    private String deadLetterExchange;

    // Completed event DLQ ayarları
    @NotBlank
    private String completedDeadLetterQueue;

    @NotBlank
    private String completedDeadLetterRoutingKey;

    // Failed event DLQ ayarları
    @NotBlank
    private String failedDeadLetterQueue;

    @NotBlank
    private String failedDeadLetterRoutingKey;
}