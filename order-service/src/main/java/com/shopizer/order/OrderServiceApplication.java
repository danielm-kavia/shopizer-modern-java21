package com.shopizer.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint for the Order Service application.
 *
 * Provides REST APIs for managing orders and order items for the Shopizer modernization effort.
 * The service is secured as an OAuth2 Resource Server validating JWTs issued by Keycloak.
 */
@SpringBootApplication
public class OrderServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Bootstraps the Spring Boot order-service application. */
    SpringApplication.run(OrderServiceApplication.class, args);
  }
}
