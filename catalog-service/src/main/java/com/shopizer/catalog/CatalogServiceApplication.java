package com.shopizer.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Catalog Service application entrypoint.
 *
 * Owns catalog data (products, categories, pricing/availability) and exposes
 * REST APIs secured with OAuth2 Resource Server (JWT).
 */
@SpringBootApplication
public class CatalogServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Bootstraps the Catalog Service Spring Boot application. */
    SpringApplication.run(CatalogServiceApplication.class, args);
  }
}
