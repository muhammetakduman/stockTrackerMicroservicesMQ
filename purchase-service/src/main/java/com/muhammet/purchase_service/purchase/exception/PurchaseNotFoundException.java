package com.muhammet.purchase_service.purchase.exception;

public class PurchaseNotFoundException extends  RuntimeException{

    public PurchaseNotFoundException(Long purchaseId){
        super("Purchase not found with ID: " + purchaseId);
    }
}
