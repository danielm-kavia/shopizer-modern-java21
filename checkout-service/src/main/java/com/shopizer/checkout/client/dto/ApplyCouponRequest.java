package com.shopizer.checkout.client.dto;

import java.math.BigDecimal;

/**
 * DTO for promotions-service /api/promotions/coupons/apply request.
 *
 * <p>Contract:
 * - code is required.
 * - subtotal/shipping/tax are >= 0.
 */
public record ApplyCouponRequest(
    String code,
    String currency,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal tax
) {}
