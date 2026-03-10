package com.shopizer.payment.service.provider.paypal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * Utilities to build PayPal request payloads.
 */
final class PayPalPayloads {

  private PayPalPayloads() {}

  static Map<String, Object> buildCreateOrderPayload(String currency, long amountMinor) {
    // PayPal expects decimal as string (e.g., "25.99") in major units.
    String value = minorToMajorString(amountMinor);
    return Map.of(
        "intent", "AUTHORIZE",
        "purchase_units", List.of(
            Map.of(
                "amount", Map.of(
                    "currency_code", currency,
                    "value", value
                )
            )
        )
    );
  }

  static String minorToMajorString(long amountMinor) {
    // Assumes 2-decimal currencies for Phase 1 (USD/EUR etc.).
    // Future: currency-aware minor unit mapping.
    BigDecimal major = BigDecimal.valueOf(amountMinor).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
    return major.toPlainString();
  }
}
