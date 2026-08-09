package com.muhammet.inventory_service.outbox.config;


import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app.outbox.publisher")
public class OutboxPublisherProperties {
    @Min(100)
    private long fixedDelayMs= 2000;
    @Min(1)
    private int batchSize=20;
    @Min(1)
    private int maxAttempts=5;
    @Min(100)
    private long confirmTimeoutMs=5000;

}
