package com.shopizer.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Customer Service application entrypoint.
 *
 * This service will eventually own customer accounts, addresses, and preferences.
 * For now it provides a minimal secured endpoint to validate the platform baseline.
 */
@SpringBootApplication
public class CustomerServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CustomerServiceApplication.class, args);
  }
}
