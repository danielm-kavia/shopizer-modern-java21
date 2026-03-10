package com.shopizer.shipping.provider.model;

import java.util.Objects;

/**
 * Normalized request for shipping providers.
 *
 * Invariants:
 * - destinationCountry non-empty
 * - currency non-empty
 * - totalWeightGrams >= 1
 */
public record RateQuoteRequest(
    String destinationCountry,
    String destinationPostalCode,
    String currency,
    int totalWeightGrams
) {

  public RateQuoteRequest {
    Objects.requireNonNull(destinationCountry, "destinationCountry");
    Objects.requireNonNull(currency, "currency");
    if (destinationCountry.isBlank()) {
      throw new IllegalArgumentException("destinationCountry must not be blank");
    }
    if (currency.isBlank()) {
      throw new IllegalArgumentException("currency must not be blank");
    }
    if (totalWeightGrams < 1) {
      throw new IllegalArgumentException("totalWeightGrams must be >= 1");
    }
  }
}
