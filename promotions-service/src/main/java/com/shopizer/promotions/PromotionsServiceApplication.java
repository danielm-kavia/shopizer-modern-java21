package com.shopizer.promotions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * promotions-service application entrypoint.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Stores coupon definitions via JPA + Flyway migrations.</li>
 *   <li>Exposes a read-only API to apply a coupon code to provided monetary amounts.</li>
 *   <li>Secures all business endpoints using JWT resource-server configuration.</li>
 * </ul>
 */
@SpringBootApplication
public class PromotionsServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Bootstraps the promotions-service Spring Boot application. */
    SpringApplication.run(PromotionsServiceApplication.class, args);
  }
}
