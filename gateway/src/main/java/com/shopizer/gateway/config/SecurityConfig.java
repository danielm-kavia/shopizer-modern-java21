package com.shopizer.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;

/**
 * Spring Security configuration for the gateway.
 *
 * <p>The gateway can be configured as an OAuth2 Resource Server validating JWTs issued by Keycloak
 * (via {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}).
 *
 * <p>In preview environments, Keycloak is often not running. If no issuer/jwk configuration is
 * provided, the gateway falls back to allowing all requests so the service can boot and pass
 * readiness checks (/, /openapi.json).
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, Environment env) {
    final String issuerUri =
        env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "");
    final String jwkSetUri =
        env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", "");

    final boolean jwtConfigured = StringUtils.hasText(issuerUri) || StringUtils.hasText(jwkSetUri);

    http.csrf(ServerHttpSecurity.CsrfSpec::disable);

    if (!jwtConfigured) {
      // Preview-friendly mode: no Keycloak configured => allow all so the app can start and be reachable.
      return http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll()).build();
    }

    // Keycloak/JWT mode.
    return http
        .authorizeExchange(exchanges -> exchanges
            // Public endpoints for preview / diagnostics / API discovery.
            .pathMatchers("/", "/health").permitAll()
            .pathMatchers("/actuator/**").permitAll()
            .pathMatchers("/openapi.json").permitAll()
            .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

            // Everything else requires a JWT (Keycloak) at the gateway edge.
            .anyExchange().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .build();
  }
}
