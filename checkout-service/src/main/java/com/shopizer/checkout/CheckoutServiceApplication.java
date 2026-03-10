package com.shopizer.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Checkout Service application.
 *
 * Phase 1 scope:
 * - Synchronously orchestrates "create order from cart items" by calling:
 *   - cart-service to read cart + items
 *   - order-service to create an order
 *
 * Security:
 * - Configured as an OAuth2 Resource Server validating Keycloak JWTs.
 */
@SpringBootApplication
public class CheckoutServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CheckoutServiceApplication.class, args);
  }
}
