package com.shopizer.promotions.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public final class PromotionsDtos {

  private PromotionsDtos() {}

  @Schema(name = "ApplyCouponRequest", description = "Request to apply a coupon code to order/cart amounts.")
  public record ApplyCouponRequest(
      @Schema(description = "Coupon code to apply (case-insensitive).", example = "SAVE10")
      @NotBlank
      String code,

      @Schema(description = "3-letter currency code (required for fixed-amount coupons).", example = "USD")
      String currency,

      @Schema(description = "Subtotal amount before discounts (>= 0).", example = "100.00")
      @NotNull
      @DecimalMin(value = "0.0", inclusive = true)
      BigDecimal subtotal,

      @Schema(description = "Shipping amount (>= 0).", example = "10.00")
      @NotNull
      @DecimalMin(value = "0.0", inclusive = true)
      BigDecimal shipping,

      @Schema(description = "Tax amount (>= 0).", example = "8.25")
      @NotNull
      @DecimalMin(value = "0.0", inclusive = true)
      BigDecimal tax
  ) {}

  @Schema(name = "ApplyCouponResponse", description = "Result of applying a coupon. Discount is applied to subtotal only (Phase 1).")
  public record ApplyCouponResponse(
      @Schema(description = "Normalized coupon code that was applied.", example = "SAVE10")
      String code,

      @Schema(description = "Currency used for evaluation (uppercased).", example = "USD")
      String currency,

      @Schema(description = "Discount applied to subtotal.", example = "10.0000")
      BigDecimal discountSubtotal,

      @Schema(description = "Total before discount = subtotal + shipping + tax.", example = "118.25")
      BigDecimal totalBeforeDiscount,

      @Schema(description = "Total after discount = totalBeforeDiscount - discountSubtotal.", example = "108.25")
      BigDecimal totalAfterDiscount
  ) {}

  @Schema(name = "ApiErrorResponse", description = "Standard API error payload.")
  public record ApiErrorResponse(
      @Schema(description = "Short machine-readable error code.", example = "COUPON_NOT_FOUND")
      String code,
      @Schema(description = "Human-readable error message.", example = "Coupon not found: SAVE10")
      String message
  ) {}
}
