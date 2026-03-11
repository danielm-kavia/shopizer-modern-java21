package com.shopizer.checkout.client.dto;

import java.math.BigDecimal;

/**
 * DTO for pricing-service /api/pricing/resolve response.
 *
 * <p>Contract (subset):
 * - extendedPrice = unitPrice * quantity, in currencyCode.
 * - unitPrice/extendedPrice are non-null on success.
 */
public record PricingResolutionResponse(
    String storeCode,
    String sku,
    String currencyCode,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal extendedPrice
) {}
