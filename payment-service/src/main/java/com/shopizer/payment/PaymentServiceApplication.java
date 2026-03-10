package com.shopizer.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint for the Payment Service application.
 *
 * Provides authorize-only payment APIs (Phase 1) backed by persistence (PostgreSQL/Flyway)
 * and secured as an OAuth2 Resource Server (JWT, Keycloak).
 */
@SpringBootApplication
public class PaymentServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Bootstraps the Spring Boot payment-service application. */
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
