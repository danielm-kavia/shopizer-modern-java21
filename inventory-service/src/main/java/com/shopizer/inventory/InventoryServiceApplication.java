package com.shopizer.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Inventory Service application.
 *
 * Provides stock availability and synchronous reservation APIs per (storeId, sku).
 */
@SpringBootApplication
public class InventoryServiceApplication {

  // PUBLIC_INTERFACE
  public static void main(String[] args) {
    /** Application entrypoint for inventory-service. */
    SpringApplication.run(InventoryServiceApplication.class, args);
  }
}
