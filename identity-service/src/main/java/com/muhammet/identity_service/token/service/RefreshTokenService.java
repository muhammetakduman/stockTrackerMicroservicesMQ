package com.muhammet.identity_service.token.service;

import com.muhammet.identity_service.exception.InvalidTokenException;
import com.muhammet.identity_service.exception.RevokedTokenException;
import com.muhammet.identity_service.exception.TokenExpiredException;
import com.muhammet.identity_service.security.jwt.JwtProperties;
import com.muhammet.identity_service.token.entity.RefreshToken;
import com.muhammet.identity_service.token.repository.RefreshTokenRepository;
import com.muhammet.identity_service.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generates a new raw refresh token, stores the hash, and returns the raw token.
     */
    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    /**
     * Validates a refresh token and returns the entity. Does NOT revoke it.
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (token.isRevoked()) {
            throw new RevokedTokenException();
        }
        if (token.isExpired()) {
            throw new TokenExpiredException();
        }
        return token;
    }

    /**
     * Rotates the refresh token: revokes old, creates new, links replacement.
     * Returns the new raw token.
     */
    @Transactional
    public String rotateRefreshToken(String oldRawToken) {
        RefreshToken old = validateRefreshToken(oldRawToken);

        String newRawToken = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        String newHash = hashToken(newRawToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUser(old.getUser());
        newToken.setTokenHash(newHash);
        newToken.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));
        newToken = refreshTokenRepository.save(newToken);

        old.setRevoked(true);
        old.setReplacedByToken(newToken);
        refreshTokenRepository.save(old);

        log.info("Refresh token rotated for user {}", old.getUser().getId());
        return newRawToken;
    }

    /**
     * Revokes a refresh token (logout).
     */
    @Transactional
    public void revokeRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    /**
     * Revokes all refresh tokens for a user (e.g., password change, forced logout).
     */
    @Transactional
    public void revokeAllTokensForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    public User getUserFromToken(String rawToken) {
        return validateRefreshToken(rawToken).getUser();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}

