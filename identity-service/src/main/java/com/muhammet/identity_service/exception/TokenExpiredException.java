package com.muhammet.identity_service.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException() {
        super("Refresh token has expired");
    }
}

