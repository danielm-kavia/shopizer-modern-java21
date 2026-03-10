package com.shopizer.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Pricing Service entrypoint.
 *
 * Exposes read-only pricing resolution APIs for:
 * - Resolving effective unit price for a store + SKU + currency at a given time
 * - Returning price breakdown (regular vs sale, sale window)
 *
 * Backing storage: PostgreSQL (shared schema "shopizer") with Flyway-managed tables.
 * Security: JWT OAuth2 resource server (Keycloak issuer configured via KEYCLOAK_ISSUER_URI).
 */
@SpringBootApplication
public class PricingServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Spring Boot entrypoint for pricing-service. */
    SpringApplication.run(PricingServiceApplication.class, args);
  }
}
