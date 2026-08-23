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
@ConfigurationProperties(prefix = "app.messaging.stock-result")
public class StockResultMessagingProperties {

    @NotBlank
    private String exchange;

    // Purchase success
    @NotBlank
    private String routingKey;

    // Purchase failure
    @NotBlank
    private String failedRoutingKey;

    // Sale success
    @NotBlank
    private String decreaseRoutingKey;

    // Sale failure
    @NotBlank
    private String decreaseFailedRoutingKey;
}