package com.shopizer.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Cart Service application entrypoint.
 *
 * Owns cart data (active shopping carts and items) and exposes REST APIs
 * secured with OAuth2 Resource Server (JWT).
 */
@SpringBootApplication
public class CartServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Bootstraps the Cart Service Spring Boot application. */
    SpringApplication.run(CartServiceApplication.class, args);
  }
}
