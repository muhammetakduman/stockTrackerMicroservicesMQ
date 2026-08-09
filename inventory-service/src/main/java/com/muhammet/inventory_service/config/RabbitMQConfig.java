package com.muhammet.inventory_service.config;

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

    private final PurchaseMessagingProperties purchaseProperties;
    private final StockResultMessagingProperties stockResultProperties;
    private final SaleMessagingProperties saleProperties;


    public RabbitMQConfig(
            PurchaseMessagingProperties purchaseProperties,
            StockResultMessagingProperties stockResultProperties,
            SaleMessagingProperties saleProperties
    ) {
        this.purchaseProperties = purchaseProperties;
        this.stockResultProperties = stockResultProperties;
        this.saleProperties = saleProperties;
    }


    // =========================================================
    // PURCHASE CREATED
    // =========================================================


    /*
     * purchase-service tarafından yayınlanan
     * PurchaseCreatedEvent mesajlarının exchange'i.
     */
    @Bean
    public TopicExchange purchaseExchange() {

        return new TopicExchange(
                purchaseProperties.getExchange(),
                true,
                false
        );
    }


    /*
     * Inventory-service'in PurchaseCreatedEvent
     * mesajlarını tükettiği ana queue.
     *
     * Mesaj retry sonrasında reject edilirse
     * Purchase DLX'e gönderilir.
     */
    @Bean
    public Queue purchaseCreatedQueue() {

        return QueueBuilder
                .durable(
                        purchaseProperties.getQueue()
                )
                .deadLetterExchange(
                        purchaseProperties.getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        purchaseProperties.getDeadLetterRoutingKey()
                )
                .build();
    }


    /*
     * purchase.exchange ile purchase queue
     * arasındaki binding.
     */
    @Bean
    public Binding purchaseCreatedBinding(

            @Qualifier("purchaseCreatedQueue")
            Queue purchaseCreatedQueue,

            @Qualifier("purchaseExchange")
            TopicExchange purchaseExchange
    ) {

        return BindingBuilder
                .bind(purchaseCreatedQueue)
                .to(purchaseExchange)
                .with(
                        purchaseProperties.getRoutingKey()
                );
    }


    /*
     * Başarısız PurchaseCreatedEvent mesajlarının
     * yönlendirileceği Dead Letter Exchange.
     */
    @Bean
    public DirectExchange purchaseDeadLetterExchange() {

        return new DirectExchange(
                purchaseProperties.getDeadLetterExchange(),
                true,
                false
        );
    }


    /*
     * Retry denemeleri tükenen PurchaseCreatedEvent
     * mesajlarının tutulduğu Dead Letter Queue.
     */
    @Bean
    public Queue purchaseCreatedDeadLetterQueue() {

        return QueueBuilder
                .durable(
                        purchaseProperties.getDeadLetterQueue()
                )
                .build();
    }


    /*
     * Purchase DLX ile Purchase DLQ arasındaki binding.
     */
    @Bean
    public Binding purchaseCreatedDeadLetterBinding(

            @Qualifier("purchaseCreatedDeadLetterQueue")
            Queue deadLetterQueue,

            @Qualifier("purchaseDeadLetterExchange")
            DirectExchange deadLetterExchange
    ) {

        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        purchaseProperties.getDeadLetterRoutingKey()
                );
    }


    // =========================================================
    // SALE CREATED
    // =========================================================


    /*
     * sales-service tarafından yayınlanan
     * SaleCreatedEvent mesajlarının exchange'i.
     *
     * sales-service:
     *
     * sale.exchange
     *      ↓
     * routing-key = sale.created
     *      ↓
     * inventory.sale-created.queue
     */
    @Bean
    public TopicExchange saleExchange() {

        return new TopicExchange(
                saleProperties.getExchange(),
                true,
                false
        );
    }


    /*
     * Inventory-service'in SaleCreatedEvent
     * mesajlarını tükettiği ana queue.
     *
     * İleride bu queue'dan gelen mesaj:
     *
     * SaleCreatedEvent
     *      ↓
     * Inbox
     *      ↓
     * StockBalance
     *      ↓
     * stok azaltma
     *
     * işlemine gidecek.
     */
    @Bean
    public Queue saleCreatedQueue() {

        return QueueBuilder
                .durable(
                        saleProperties.getQueue()
                )
                .deadLetterExchange(
                        saleProperties.getDeadLetterExchange()
                )
                .deadLetterRoutingKey(
                        saleProperties.getDeadLetterRoutingKey()
                )
                .build();
    }


    /*
     * sale.exchange ile SaleCreatedEvent queue'su
     * arasındaki binding.
     */
    @Bean
    public Binding saleCreatedBinding(

            @Qualifier("saleCreatedQueue")
            Queue saleCreatedQueue,

            @Qualifier("saleExchange")
            TopicExchange saleExchange
    ) {

        return BindingBuilder
                .bind(saleCreatedQueue)
                .to(saleExchange)
                .with(
                        saleProperties.getRoutingKey()
                );
    }


    /*
     * Retry denemeleri bittikten sonra başarısız
     * SaleCreatedEvent mesajlarının gönderileceği
     * Dead Letter Exchange.
     */
    @Bean
    public DirectExchange saleDeadLetterExchange() {

        return new DirectExchange(
                saleProperties.getDeadLetterExchange(),
                true,
                false
        );
    }


    /*
     * İşlenemeyen SaleCreatedEvent mesajlarının
     * tutulacağı Dead Letter Queue.
     */
    @Bean
    public Queue saleCreatedDeadLetterQueue() {

        return QueueBuilder
                .durable(
                        saleProperties.getDeadLetterQueue()
                )
                .build();
    }


    /*
     * Sale DLX ile Sale DLQ arasındaki binding.
     */
    @Bean
    public Binding saleCreatedDeadLetterBinding(

            @Qualifier("saleCreatedDeadLetterQueue")
            Queue deadLetterQueue,

            @Qualifier("saleDeadLetterExchange")
            DirectExchange deadLetterExchange
    ) {

        return BindingBuilder
                .bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(
                        saleProperties.getDeadLetterRoutingKey()
                );
    }


    // =========================================================
    // STOCK RESULT
    // =========================================================


    /*
     * Inventory-service'in stok işlemlerinin
     * sonuçlarını yayınladığı ortak exchange.
     *
     * Örneğin:
     *
     * stock.increase.completed
     * stock.increase.failed
     *
     * ve birazdan:
     *
     * stock.decrease.completed
     * stock.decrease.failed
     *
     * eventleri buraya yayınlanabilir.
     */
    @Bean
    public TopicExchange stockResultExchange() {

        return new TopicExchange(
                stockResultProperties.getExchange(),
                true,
                false
        );
    }


    // =========================================================
    // JSON MESSAGE CONVERTER
    // =========================================================


    /*
     * RabbitMQ mesajlarının JSON <-> Java record/class
     * dönüşümünü gerçekleştirir.
     *
     * Spring AMQP 4.x / Jackson 3 kullanıyoruz.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {

        return new JacksonJsonMessageConverter();
    }
}