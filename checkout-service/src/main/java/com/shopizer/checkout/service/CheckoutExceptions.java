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

  /** Raised when shipping quotes cannot be obtained due to downstream failures. */
  public static class CheckoutShippingQuoteException extends RuntimeException {
    public CheckoutShippingQuoteException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /** Raised when caller selects a shipping quote that is not present in the returned set. */
  public static class CheckoutShippingSelectionException extends RuntimeException {
    public CheckoutShippingSelectionException(String message) {
      super(message);
    }
  }
}
