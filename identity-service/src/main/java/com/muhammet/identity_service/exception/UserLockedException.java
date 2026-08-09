package com.muhammet.identity_service.exception;

public class UserLockedException extends RuntimeException {
    public UserLockedException() {
        super("User account is locked");
    }
}

