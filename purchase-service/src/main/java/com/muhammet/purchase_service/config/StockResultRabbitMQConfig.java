package com.muhammet.purchase_service.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StockResultRabbitMQConfig {

    private final StockResultMessagingProperties properties;

    public StockResultRabbitMQConfig(
            StockResultMessagingProperties properties
    ) {
        this.properties = properties;
    }

    /*
     * Inventory-service tarafından success ve failed
     * sonuç eventlerinin yayınlandığı ortak exchange.
     */
    @Bean
    public TopicExchange stockResultExchange() {
        return new TopicExchange(
                properties.getExchange(),
                true,
                false
        );
    }

    /*
     * Completed ve failed sonuç mesajlarının,
     * tüketilemediğinde yönlendirileceği ortak DLX.
     */
    @Bean
    public DirectExchange stockResultDeadLetterExchange() {
        return new DirectExchange(
                properties.getDeadLetterExchange(),
                true,
                false
        );
    }

    /*
     * StockIncreaseCompletedEvent ana queue'su.
     */
    @Bean
    public Queue stockIncreaseCompletedQueue() {
        return QueueBuilder
                .durable(properties.getQueue())
                .deadLetterExchange(
                        properties.getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        properties
                                .getCompletedDeadLetterRoutingKey()
                )
                .build();
    }

    @Bean
    public Binding stockIncreaseCompletedBinding(
            @Qualifier("stockIncreaseCompletedQueue")
            Queue completedQueue,

            @Qualifier("stockResultExchange")
            TopicExchange stockResultExchange
    ) {
        return BindingBuilder
                .bind(completedQueue)
                .to(stockResultExchange)
                .with(properties.getRoutingKey());
    }

    /*
     * Completed event retry sonrasında
     * işlenemezse burada tutulur.
     */
    @Bean
    public Queue stockIncreaseCompletedDeadLetterQueue() {
        return QueueBuilder
                .durable(
                        properties
                                .getCompletedDeadLetterQueue()
                )
                .build();
    }

    @Bean
    public Binding stockIncreaseCompletedDeadLetterBinding(
            @Qualifier(
                    "stockIncreaseCompletedDeadLetterQueue"
            )
            Queue completedDeadLetterQueue,

            @Qualifier("stockResultDeadLetterExchange")
            DirectExchange deadLetterExchange
    ) {
        return BindingBuilder
                .bind(completedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        properties
                                .getCompletedDeadLetterRoutingKey()
                );
    }

    /*
     * StockIncreaseFailedEvent ana queue'su.
     */
    @Bean
    public Queue stockIncreaseFailedQueue() {
        return QueueBuilder
                .durable(properties.getFailedQueue())
                .deadLetterExchange(
                        properties.getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        properties
                                .getFailedDeadLetterRoutingKey()
                )
                .build();
    }

    @Bean
    public Binding stockIncreaseFailedBinding(
            @Qualifier("stockIncreaseFailedQueue")
            Queue failedQueue,

            @Qualifier("stockResultExchange")
            TopicExchange stockResultExchange
    ) {
        return BindingBuilder
                .bind(failedQueue)
                .to(stockResultExchange)
                .with(properties.getFailedRoutingKey());
    }

    /*
     * Failed event purchase-service tarafından
     * işlenemezse burada tutulur.
     */
    @Bean
    public Queue stockIncreaseFailedDeadLetterQueue() {
        return QueueBuilder
                .durable(
                        properties.getFailedDeadLetterQueue()
                )
                .build();
    }

    @Bean
    public Binding stockIncreaseFailedDeadLetterBinding(
            @Qualifier(
                    "stockIncreaseFailedDeadLetterQueue"
            )
            Queue failedDeadLetterQueue,

            @Qualifier("stockResultDeadLetterExchange")
            DirectExchange deadLetterExchange
    ) {
        return BindingBuilder
                .bind(failedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        properties
                                .getFailedDeadLetterRoutingKey()
                );
    }
}