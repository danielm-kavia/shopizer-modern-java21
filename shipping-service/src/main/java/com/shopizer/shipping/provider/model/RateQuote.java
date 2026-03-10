package com.shopizer.shipping.provider.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Normalized quote option from a provider.
 *
 * Invariants:
 * - amount >= 0
 * - provider/serviceLevel/serviceName non-empty
 */
public record RateQuote(
    String provider,
    String serviceLevel,
    String serviceName,
    BigDecimal amount
) {
  public RateQuote {
    Objects.requireNonNull(provider, "provider");
    Objects.requireNonNull(serviceLevel, "serviceLevel");
    Objects.requireNonNull(serviceName, "serviceName");
    Objects.requireNonNull(amount, "amount");
    if (provider.isBlank() || serviceLevel.isBlank() || serviceName.isBlank()) {
      throw new IllegalArgumentException("provider/serviceLevel/serviceName must not be blank");
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }
  }
}
