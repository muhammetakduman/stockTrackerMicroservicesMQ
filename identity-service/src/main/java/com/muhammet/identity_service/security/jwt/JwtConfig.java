package com.muhammet.identity_service.security.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public RsaKeyPair rsaKeyPair(JwtProperties props) throws Exception {
        RSAPublicKey publicKey = loadPublicKey(props);
        RSAPrivateKey privateKey = loadPrivateKey(props);
        return new RsaKeyPair(publicKey, privateKey);
    }

    @Bean
    public JwtDecoder jwtDecoder(RsaKeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey(keyPair.publicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RsaKeyPair keyPair) {
        RSAKey jwk = new RSAKey.Builder(keyPair.publicKey())
                .privateKey(keyPair.privateKey())
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    private RSAPublicKey loadPublicKey(JwtProperties props) throws Exception {
        String pem = StreamUtils.copyToString(
                props.getPublicKeyLocation().getInputStream(), StandardCharsets.UTF_8);
        String stripped = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(stripped);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private RSAPrivateKey loadPrivateKey(JwtProperties props) throws Exception {
        String pem = StreamUtils.copyToString(
                props.getPrivateKeyLocation().getInputStream(), StandardCharsets.UTF_8);
        String stripped = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(stripped);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }
}

