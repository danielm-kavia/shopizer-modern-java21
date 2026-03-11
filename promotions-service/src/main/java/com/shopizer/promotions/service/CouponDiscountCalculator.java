package com.shopizer.promotions.service;

import com.shopizer.promotions.domain.CouponDefinition;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure discount calculation logic for applying a coupon to an amount.
 *
 * <p>Contract:
 * <ul>
 *   <li>Inputs: coupon definition, currency (nullable for percentage), and pre-discount amount >= 0</li>
 *   <li>Output: discount amount in same currency, 0 <= discount <= amount, scale 4</li>
 *   <li>No I/O side effects</li>
 * </ul>
 */
public final class CouponDiscountCalculator {

  private static final int MONEY_SCALE = 4;

  private CouponDiscountCalculator() {}

  // PUBLIC_INTERFACE
  public static BigDecimal calculateDiscount(CouponDefinition coupon, String currency, BigDecimal amount) {
    /** Calculates a safe discount amount clamped to [0, amount]. */
    if (coupon == null) {
      throw new IllegalArgumentException("coupon must not be null");
    }
    if (amount == null) {
      throw new IllegalArgumentException("amount must not be null");
    }
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("amount must be >= 0");
    }

    BigDecimal discount;
    switch (coupon.getType()) {
      case PERCENTAGE -> {
        // percentageOff is expressed as "10" for 10% (not 0.10)
        BigDecimal pct = coupon.getPercentageOff();
        discount = amount.multiply(pct).divide(BigDecimal.valueOf(100), MONEY_SCALE, RoundingMode.HALF_UP);
      }
      case FIXED_AMOUNT -> {
        String expectedCurrency = coupon.getCurrency();
        if (currency == null || currency.isBlank()) {
          throw new PromotionsExceptions.InvalidApplyRequestException("currency is required when applying FIXED_AMOUNT coupons");
        }
        if (expectedCurrency != null && !expectedCurrency.equalsIgnoreCase(currency)) {
          throw new PromotionsExceptions.CurrencyMismatchException(coupon.getCode(), expectedCurrency, currency);
        }
        discount = coupon.getAmountOff();
      }
      default -> throw new IllegalStateException("Unsupported coupon type: " + coupon.getType());
    }

    if (discount == null) {
      discount = BigDecimal.ZERO;
    }
    if (discount.signum() < 0) {
      discount = BigDecimal.ZERO;
    }

    // Clamp: never exceed the amount.
    if (discount.compareTo(amount) > 0) {
      discount = amount;
    }

    return discount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
  }
}
