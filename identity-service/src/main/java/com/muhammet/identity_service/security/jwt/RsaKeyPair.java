package com.muhammet.identity_service.security.jwt;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record RsaKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
}

