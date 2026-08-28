package com.muhammet.sales_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * JWT "roles" claim'ini Spring Security authority'lerine map eder.
     *
     * identity-service JwtService.generateAccessToken() içinde:
     *   .claim("roles", roles)  →  ["SALES_USER"] veya ["ADMIN"] vb.
     *
     * Spring'in default converter yalnızca "scope"/"scp" claim'ini okur.
     * Bu bean olmadan hasRole("SALES_USER") HİÇ çalışmaz.
     *
     * Prefix "ROLE_" eklenir → hasRole("SALES_USER") = hasAuthority("ROLE_SALES_USER")
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        // Default "scope"/"scp" yerine identity-service'in kullandığı "roles" claim'i
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        // hasRole("SALES_USER") → "ROLE_SALES_USER" arar — prefix zorunlu
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth ->
                        auth
                                /*
                                 * Health check JWT istemesin.
                                 */
                                .requestMatchers(
                                        "/actuator/health",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                /*
                                 * GET /api/v1/sales  (tüm satışlar) — ADMIN only.
                                 * SALES_USER kendi satışları için /my kullanır.
                                 * Diğer endpoint'ler @PreAuthorize ile korunur.
                                 */
                                .anyRequest()
                                .authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
}

