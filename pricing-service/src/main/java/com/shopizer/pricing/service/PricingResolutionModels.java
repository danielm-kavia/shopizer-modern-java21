package com.shopizer.pricing.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Models for PricingResolutionFlow.
 */
public final class PricingResolutionModels {

  private PricingResolutionModels() {}

  /**
   * PUBLIC_INTERFACE
   *
   * Immutable request for pricing resolution.
   */
  public record PricingResolutionRequest(
      String storeCode,
      String sku,
      String currencyCode,
      int quantity,
      OffsetDateTime at
  ) { }

  /**
   * PUBLIC_INTERFACE
   *
   * Immutable response for pricing resolution.
   */
  public record PricingResolution(
      String storeCode,
      String sku,
      String currencyCode,
      int quantity,
      OffsetDateTime evaluatedAt,
      BigDecimal unitPrice,
      BigDecimal extendedPrice,
      PricingResolutionFlow.PriceType appliedPriceType,
      BigDecimal regularUnitPrice,
      BigDecimal saleUnitPrice,
      OffsetDateTime saleStartAt,
      OffsetDateTime saleEndAt
  ) { }
}
