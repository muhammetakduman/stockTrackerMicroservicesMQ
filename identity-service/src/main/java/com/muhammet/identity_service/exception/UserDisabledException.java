package com.muhammet.identity_service.exception;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException() {
        super("User account is disabled");
    }
}

