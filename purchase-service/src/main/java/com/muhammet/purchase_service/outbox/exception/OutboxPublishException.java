package com.muhammet.purchase_service.outbox.exception;

public class OutboxPublishException extends RuntimeException{
    public OutboxPublishException(String message) {
        super(message);
    }
    public  OutboxPublishException(
            String message, Throwable cause
    ){
        super(message, cause);
    }
}
