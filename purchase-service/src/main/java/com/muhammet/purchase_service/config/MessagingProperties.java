package com.muhammet.purchase_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.messaging.purchase")
public class MessagingProperties {

    private String exchange;
    private String routingKey;
}

