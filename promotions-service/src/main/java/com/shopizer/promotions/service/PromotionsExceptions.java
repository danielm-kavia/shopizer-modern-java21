package com.shopizer.promotions.service;

/**
 * Exception set for promotions-service.
 *
 * <p>These exceptions are mapped to API errors in the web layer.</p>
 */
public final class PromotionsExceptions {

  private PromotionsExceptions() {}

  public static class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code) {
      super("Coupon not found: " + code);
    }
  }

  public static class CouponInactiveException extends RuntimeException {
    public CouponInactiveException(String code) {
      super("Coupon is inactive or not currently valid: " + code);
    }
  }

  public static class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String code, String expected, String actual) {
      super("Coupon currency mismatch for " + code + " (expected " + expected + ", got " + actual + ")");
    }
  }

  public static class InvalidApplyRequestException extends RuntimeException {
    public InvalidApplyRequestException(String message) {
      super(message);
    }
  }
}
