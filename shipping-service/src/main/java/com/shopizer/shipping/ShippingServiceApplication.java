package com.shopizer.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Shipping Service (Phase 1).
 *
 * Provides authenticated REST endpoints for shipping rate quotes, backed by stub in-memory providers.
 * Optionally logs quote requests/responses to a database using Flyway + JPA when configured.
 */
@SpringBootApplication
public class ShippingServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Application entrypoint for shipping-service. */
    SpringApplication.run(ShippingServiceApplication.class, args);
  }
}
