package com.muhammet.identity_service.exception;

public class RevokedTokenException extends RuntimeException {
    public RevokedTokenException() {
        super("Refresh token has been revoked");
    }
}

