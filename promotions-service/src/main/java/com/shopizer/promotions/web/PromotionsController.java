package com.shopizer.promotions.web;

import com.shopizer.promotions.service.ApplyCouponFlow;
import com.shopizer.promotions.web.dto.PromotionsDtos;
import com.shopizer.promotions.web.dto.PromotionsDtos.ApplyCouponRequest;
import com.shopizer.promotions.web.dto.PromotionsDtos.ApplyCouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Promotions REST endpoints.
 *
 * <p>This service is intentionally read-only for Phase 1: callers can apply coupon codes but
 * cannot create/update coupon definitions via API (seeded/managed out-of-band).</p>
 */
@RestController
@RequestMapping(path = "/api/promotions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Promotions", description = "Coupon apply endpoints")
public class PromotionsController {

  private final ApplyCouponFlow applyCouponFlow;

  public PromotionsController(ApplyCouponFlow applyCouponFlow) {
    this.applyCouponFlow = applyCouponFlow;
  }

  // PUBLIC_INTERFACE
  @PostMapping(path = "/coupons/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Apply coupon code (read-only)",
      description = "Applies a coupon code to the provided subtotal/shipping/tax amounts. Phase 1 applies discounts to subtotal only."
  )
  public ApplyCouponResponse applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
    /** HTTP entrypoint for applying a coupon. Validates input and delegates to ApplyCouponFlow. */
    var result = applyCouponFlow.apply(new ApplyCouponFlow.ApplyCouponRequest(
        request.code(),
        normalizeCurrency(request.currency()),
        request.subtotal(),
        request.shipping(),
        request.tax()
    ));

    return new PromotionsDtos.ApplyCouponResponse(
        result.code(),
        result.currency(),
        result.discountSubtotal(),
        result.totalBeforeDiscount(),
        result.totalAfterDiscount()
    );
  }

  private String normalizeCurrency(String currency) {
    if (currency == null) {
      return null;
    }
    String trimmed = currency.trim();
    if (trimmed.isBlank()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }
}
