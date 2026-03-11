package com.shopizer.tax;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Tax Service application.
 *
 * <p>Phase 1 scope: maintain a single percentage tax rate per store and provide a read-only REST
 * endpoint to calculate tax for a cart/order payload.</p>
 */
@SpringBootApplication
public class TaxServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Application entry point for tax-service. */
    SpringApplication.run(TaxServiceApplication.class, args);
  }
}
