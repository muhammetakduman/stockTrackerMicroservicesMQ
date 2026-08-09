package com.muhammet.inventory_service.stock.exception;

import lombok.Getter;

@Getter
public class StockProcessingException extends RuntimeException{
    private final String errorCode;

    public StockProcessingException(
            String errorCode,
            String message
    ){
        super(message);
        if (errorCode == null || errorCode.isBlank()){
            throw new IllegalArgumentException("errorCode cannot be null or blank");
        }
        this.errorCode = errorCode;
    }
}
