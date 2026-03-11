package com.shopizer.checkout.client.dto;

import java.math.BigDecimal;

/**
 * DTO for promotions-service /api/promotions/coupons/apply response.
 *
 * <p>Contract:
 * - discountSubtotal is the discount applied to subtotal (Phase 1).
 * - totals are subtotal + shipping + tax, before/after discount.
 */
public record ApplyCouponResponse(
    String code,
    String currency,
    BigDecimal discountSubtotal,
    BigDecimal totalBeforeDiscount,
    BigDecimal totalAfterDiscount
) {}
