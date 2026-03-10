package com.shopizer.inventory.service;

/**
 * Inventory domain exceptions.
 */
public final class InventoryExceptions {

  private InventoryExceptions() {}

  public static class NotFound extends RuntimeException {
    public NotFound(String message) {
      super(message);
    }
  }

  public static class InsufficientAvailable extends RuntimeException {
    public InsufficientAvailable(String message) {
      super(message);
    }
  }

  public static class InvalidQuantity extends RuntimeException {
    public InvalidQuantity(String message) {
      super(message);
    }
  }
}
