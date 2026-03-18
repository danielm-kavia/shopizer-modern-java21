package com.shopizer.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

/**
 * Spring Security configuration for catalog-service.
 *
 * <p>The catalog-service supports OAuth2 Resource Server mode (JWT) when Keycloak settings are
 * provided via:
 * <ul>
 *   <li>{@code spring.security.oauth2.resourceserver.jwt.issuer-uri}</li>
 *   <li>or {@code spring.security.oauth2.resourceserver.jwt.jwk-set-uri}</li>
 * </ul>
 *
 * <p>In preview environments, Keycloak may not be running. If no JWT configuration is provided,
 * we allow all requests so the service can boot and the gateway can route to it.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment env) throws Exception {
    final String issuerUri =
        env.getProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri", "");
    final String jwkSetUri =
        env.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", "");

    final boolean jwtConfigured = StringUtils.hasText(issuerUri) || StringUtils.hasText(jwkSetUri);

    http.csrf(csrf -> csrf.disable());

    if (!jwtConfigured) {
      // Preview-friendly mode: Keycloak not configured => allow all.
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }

    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/health").permitAll()
            // springdoc endpoints (useful in dev; still behind auth if desired later)
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

    return http.build();
  }
}
