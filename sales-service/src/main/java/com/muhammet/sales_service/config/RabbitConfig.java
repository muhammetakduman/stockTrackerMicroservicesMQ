package com.muhammet.sales_service.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {


    @Bean
    public TopicExchange saleExchange(
            @Value("${app.messaging.sale.exchange}")
            String exchangeName
    ) {

        return new TopicExchange(
                exchangeName,
                true,
                false
        );
    }
}