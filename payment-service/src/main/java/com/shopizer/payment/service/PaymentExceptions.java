package com.shopizer.payment.service;

/**
 * Exception types for payment-service flows.
 */
public final class PaymentExceptions {

  private PaymentExceptions() {}

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
      super(message);
    }
  }

  public static class ValidationException extends RuntimeException {
    public ValidationException(String message) {
      super(message);
    }
  }

  public static class ProviderException extends RuntimeException {
    public ProviderException(String message, Throwable cause) {
      super(message, cause);
    }

    public ProviderException(String message) {
      super(message);
    }
  }

  public static class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
      super(message);
    }
  }
}
