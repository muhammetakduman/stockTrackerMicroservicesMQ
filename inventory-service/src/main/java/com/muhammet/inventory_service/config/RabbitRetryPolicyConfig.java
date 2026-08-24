package com.muhammet.inventory_service.config;

import com.muhammet.inventory_service.stock.exception.StockProcessingException;

import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitRetryPolicyConfig {

    private static final String INSUFFICIENT_AVAILABLE_STOCK =
            "INSUFFICIENT_AVAILABLE_STOCK";

    private static final String STOCK_BALANCE_NOT_FOUND =
            "STOCK_BALANCE_NOT_FOUND";


    @Bean
    public RabbitListenerRetrySettingsCustomizer
    rabbitListenerRetrySettingsCustomizer() {

        return retrySettings ->
                retrySettings.setExceptionPredicate(
                        this::isRetryable
                );
    }


    /*
     * true  -> retry et
     * false -> retry etme, recoverer'a geç
     */
    private boolean isRetryable(
            Throwable throwable
    ) {

        StockProcessingException stockException =
                findStockProcessingException(
                        throwable
                );


        /*
         * StockProcessingException değilse şimdilik
         * teknik hata kabul ediyoruz ve retry ediyoruz.
         */
        if (stockException == null) {
            return true;
        }


        return switch (
                stockException.getErrorCode()
                ) {

            /*
             * Business failures.
             *
             * Tekrar denemenin anlamı yok.
             */
            case INSUFFICIENT_AVAILABLE_STOCK,
                 STOCK_BALANCE_NOT_FOUND -> false;


            /*
             * Tanımadığımız StockProcessingException'larda
             * güvenli tarafta kalıp mevcut retry davranışını
             * koruyoruz.
             */
            default -> true;
        };
    }


    /*
     * Rabbit listener exception'ı genellikle
     * ListenerExecutionFailedException gibi wrapper'ların
     * içerisinde gelir.
     *
     * Bu yüzden yalnızca throwable instanceof kontrolü
     * yapmak yerine cause chain'i geziyoruz.
     */
    private StockProcessingException
    findStockProcessingException(
            Throwable throwable
    ) {

        Throwable current =
                throwable;


        while (current != null) {

            if (current
                    instanceof StockProcessingException exception) {

                return exception;
            }


            if (current.getCause()
                    == current) {

                break;
            }


            current =
                    current.getCause();
        }


        return null;
    }
}