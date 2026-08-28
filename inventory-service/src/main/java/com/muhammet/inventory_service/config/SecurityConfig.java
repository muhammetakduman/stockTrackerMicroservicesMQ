package com.muhammet.inventory_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * JWT "roles" claim'ini Spring Security authority'lerine map eder.
     *
     * identity-service JwtService.generateAccessToken() içinde:
     *   .claim("roles", roles)  →  ["ADMIN"], ["STOCK_MANAGER"] vb.
     *
     * Spring'in default converter yalnızca "scope"/"scp" claim'ini okur.
     * Bu bean olmadan hasRole("ADMIN") HİÇ çalışmaz.
     *
     * Prefix "ROLE_" eklenir → hasRole("ADMIN") = hasAuthority("ROLE_ADMIN")
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        // Default "scope"/"scp" yerine identity-service'in kullandığı "roles" claim'i
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        // hasRole("ADMIN") → "ROLE_ADMIN" arar — prefix zorunlu
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

                                // Actuator
                                .requestMatchers(
                                        "/actuator/**"
                                )
                                .permitAll()

                                // Swagger / OpenAPI
                                .requestMatchers(
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()

                                // Business endpoints
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