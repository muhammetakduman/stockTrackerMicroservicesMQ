package com.muhammet.identity_service.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtProperties {

    private Resource privateKeyLocation;
    private Resource publicKeyLocation;
    private long accessTokenExpirationSeconds = 900;
    private long refreshTokenExpirationSeconds = 604800;
}

