package com.shopizer.checkout.service;

/**
 * Checkout domain exceptions used to surface consistent errors from the orchestration flow.
 */
public final class CheckoutExceptions {

  private CheckoutExceptions() {}

  public static class CheckoutValidationException extends RuntimeException {
    public CheckoutValidationException(String message) {
      super(message);
    }
  }

  public static class CheckoutOrchestrationException extends RuntimeException {
    public CheckoutOrchestrationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class CheckoutInventoryReservationException extends RuntimeException {
    public CheckoutInventoryReservationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
