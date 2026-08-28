package com.muhammet.sales_service.sale.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

/**
 * JWT "sub" claim'inden authenticated kullanıcının UUID'sini çözer.
 * <p>
 * identity-service JWT'de subject = user UUID (toString).
 * Claim adı: "sub"
 */
@Component
public class AuthenticatedSeller {

    /**
     * Mevcut güvenlik bağlamındaki JWT'den kullanıcı UUID'sini döndürür.
     *
     * @return authenticated kullanıcının UUID'si
     * @throws ResponseStatusException 401 — JWT yoksa veya sub claim geçersizse
     */
    public UUID getCurrentSellerId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof Jwt jwt)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication principal"
            );
        }

        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT subject claim is missing"
            );
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT subject is not a valid UUID"
            );
        }
    }
}

