package com.muhammet.sales_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final StockResultMessagingProperties
            stockResultProperties;


    public RabbitMQConfig(
            StockResultMessagingProperties stockResultProperties
    ) {

        this.stockResultProperties =
                stockResultProperties;
    }


    // =========================================================
    // STOCK RESULT EXCHANGE
    // =========================================================

    @Bean
    public TopicExchange stockResultExchange() {

        return new TopicExchange(
                stockResultProperties.getExchange(),
                true,
                false
        );
    }


    // =========================================================
    // STOCK DECREASE COMPLETED
    // =========================================================

    @Bean
    public Queue stockDecreaseCompletedQueue() {

        return QueueBuilder
                .durable(
                        stockResultProperties
                                .getCompletedQueue()
                )
                .deadLetterExchange(
                        stockResultProperties
                                .getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        stockResultProperties
                                .getCompletedDeadLetterRoutingKey()
                )
                .build();
    }


    @Bean
    public Binding stockDecreaseCompletedBinding(

            @Qualifier("stockDecreaseCompletedQueue")
            Queue queue,

            @Qualifier("stockResultExchange")
            TopicExchange exchange
    ) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(
                        stockResultProperties
                                .getCompletedRoutingKey()
                );
    }


    // =========================================================
    // STOCK DECREASE FAILED
    // =========================================================

    @Bean
    public Queue stockDecreaseFailedQueue() {

        return QueueBuilder
                .durable(
                        stockResultProperties
                                .getFailedQueue()
                )
                .deadLetterExchange(
                        stockResultProperties
                                .getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        stockResultProperties
                                .getFailedDeadLetterRoutingKey()
                )
                .build();
    }


    @Bean
    public Binding stockDecreaseFailedBinding(

            @Qualifier("stockDecreaseFailedQueue")
            Queue queue,

            @Qualifier("stockResultExchange")
            TopicExchange exchange
    ) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(
                        stockResultProperties
                                .getFailedRoutingKey()
                );
    }


    // =========================================================
    // DEAD LETTER EXCHANGE
    // =========================================================

    @Bean
    public DirectExchange salesStockResultDeadLetterExchange() {

        return new DirectExchange(
                stockResultProperties
                        .getDeadLetterExchange(),
                true,
                false
        );
    }


    // =========================================================
    // COMPLETED DLQ
    // =========================================================

    @Bean
    public Queue stockDecreaseCompletedDeadLetterQueue() {

        return QueueBuilder
                .durable(
                        stockResultProperties
                                .getCompletedDlq()
                )
                .build();
    }


    @Bean
    public Binding stockDecreaseCompletedDeadLetterBinding(

            @Qualifier(
                    "stockDecreaseCompletedDeadLetterQueue"
            )
            Queue queue,

            @Qualifier(
                    "salesStockResultDeadLetterExchange"
            )
            DirectExchange exchange
    ) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(
                        stockResultProperties
                                .getCompletedDeadLetterRoutingKey()
                );
    }


    // =========================================================
    // FAILED DLQ
    // =========================================================

    @Bean
    public Queue stockDecreaseFailedDeadLetterQueue() {

        return QueueBuilder
                .durable(
                        stockResultProperties
                                .getFailedDlq()
                )
                .build();
    }


    @Bean
    public Binding stockDecreaseFailedDeadLetterBinding(

            @Qualifier(
                    "stockDecreaseFailedDeadLetterQueue"
            )
            Queue queue,

            @Qualifier(
                    "salesStockResultDeadLetterExchange"
            )
            DirectExchange exchange
    ) {

        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(
                        stockResultProperties
                                .getFailedDeadLetterRoutingKey()
                );
    }


    // =========================================================
    // JSON
    // =========================================================

    @Bean
    public MessageConverter jsonMessageConverter() {

        return new JacksonJsonMessageConverter();
    }
}