package com.shopizer.promotions.service;

import com.shopizer.promotions.domain.CouponDefinition;
import com.shopizer.promotions.repo.CouponDefinitionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ApplyCouponFlow - canonical flow for applying a coupon code to provided amounts.
 *
 * <p>Contract:
 * <ul>
 *   <li>Inputs:
 *     <ul>
 *       <li>code: non-blank coupon code</li>
 *       <li>currency: optional for percentage coupons; required for fixed-amount coupons</li>
 *       <li>subtotal, shipping, tax: each must be >= 0</li>
 *     </ul>
 *   </li>
 *   <li>Behavior:
 *     <ul>
 *       <li>Find coupon by code (case-insensitive via normalization)</li>
 *       <li>Verify active and within validity window (startsAt/endsAt)</li>
 *       <li>Compute discount on subtotal only (Phase 1 invariant)</li>
 *     </ul>
 *   </li>
 *   <li>Outputs:
 *     <ul>
 *       <li>discountSubtotal >= 0 and <= subtotal</li>
 *       <li>totalAfterDiscount = subtotal + shipping + tax - discountSubtotal</li>
 *     </ul>
 *   </li>
 *   <li>Errors:
 *     <ul>
 *       <li>CouponNotFoundException, CouponInactiveException, CurrencyMismatchException, InvalidApplyRequestException</li>
 *     </ul>
 *   </li>
 * </ul>
 */
@Service
public class ApplyCouponFlow {

  private static final Logger log = LoggerFactory.getLogger(ApplyCouponFlow.class);

  private final CouponDefinitionRepository couponRepo;

  public ApplyCouponFlow(CouponDefinitionRepository couponRepo) {
    this.couponRepo = couponRepo;
  }

  public record ApplyCouponRequest(
      String code,
      String currency,
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal tax
  ) {}

  public record ApplyCouponResult(
      String code,
      String currency,
      BigDecimal discountSubtotal,
      BigDecimal totalBeforeDiscount,
      BigDecimal totalAfterDiscount
  ) {}

  // PUBLIC_INTERFACE
  public ApplyCouponResult apply(ApplyCouponRequest request) {
    /** Applies a coupon to the request amounts using the canonical promotions flow. */
    String code = normalizeCode(request.code());
    validateAmounts(request);

    log.info("ApplyCouponFlow.start code={} currency={} subtotal={} shipping={} tax={}",
        code, safeUpper(request.currency()), request.subtotal(), request.shipping(), request.tax());

    CouponDefinition coupon = couponRepo.findByCode(code)
        .orElseThrow(() -> new PromotionsExceptions.CouponNotFoundException(code));

    ensureCouponActiveNow(coupon);

    BigDecimal discountSubtotal = CouponDiscountCalculator.calculateDiscount(coupon, request.currency(), request.subtotal());

    BigDecimal totalBefore = request.subtotal().add(request.shipping()).add(request.tax());
    BigDecimal totalAfter = totalBefore.subtract(discountSubtotal);

    ApplyCouponResult result = new ApplyCouponResult(
        code,
        safeUpper(request.currency()),
        discountSubtotal,
        totalBefore,
        totalAfter
    );

    log.info("ApplyCouponFlow.success code={} discountSubtotal={} totalBefore={} totalAfter={}",
        code, discountSubtotal, totalBefore, totalAfter);

    return result;
  }

  private void validateAmounts(ApplyCouponRequest request) {
    if (request == null) {
      throw new PromotionsExceptions.InvalidApplyRequestException("request must not be null");
    }
    if (normalizeCode(request.code()).isBlank()) {
      throw new PromotionsExceptions.InvalidApplyRequestException("code must be provided");
    }
    if (request.subtotal() == null || request.shipping() == null || request.tax() == null) {
      throw new PromotionsExceptions.InvalidApplyRequestException("subtotal, shipping, and tax must be provided");
    }
    if (request.subtotal().signum() < 0 || request.shipping().signum() < 0 || request.tax().signum() < 0) {
      throw new PromotionsExceptions.InvalidApplyRequestException("subtotal, shipping, and tax must be >= 0");
    }
  }

  private void ensureCouponActiveNow(CouponDefinition coupon) {
    if (!coupon.isActive()) {
      throw new PromotionsExceptions.CouponInactiveException(coupon.getCode());
    }
    Instant now = Instant.now();
    if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
      throw new PromotionsExceptions.CouponInactiveException(coupon.getCode());
    }
    if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
      throw new PromotionsExceptions.CouponInactiveException(coupon.getCode());
    }
  }

  private String normalizeCode(String code) {
    return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
  }

  private String safeUpper(String currency) {
    return currency == null ? null : currency.trim().toUpperCase(Locale.ROOT);
  }
}
