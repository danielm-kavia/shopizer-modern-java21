package com.shopizer.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Spring Security configuration for the gateway.
 *
 * The gateway is configured as an OAuth2 Resource Server validating JWTs issued by Keycloak.
 * Issuer/JWK configuration is provided via application.yml (spring.security.oauth2.resourceserver.jwt.*).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // Keep this intentionally simple for the scaffold baseline.
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchanges -> exchanges
            // Public endpoints for preview / diagnostics / API discovery.
            .pathMatchers("/", "/health").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/openapi.json").permitAll()
            .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

            // Everything else requires a JWT (Keycloak) at the gateway edge.
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
  }
}
