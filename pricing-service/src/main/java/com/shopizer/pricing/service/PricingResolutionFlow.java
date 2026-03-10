package com.shopizer.pricing.service;

import com.shopizer.pricing.domain.Price;
import com.shopizer.pricing.repo.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * PricingResolutionFlow
 *
 * Contract:
 * Inputs:
 * - storeCode: required, non-blank
 * - sku: required, non-blank
 * - currencyCode: required 3-letter code, uppercase recommended
 * - quantity: required > 0
 * - at: optional timestamp for evaluation; if null, uses current time via Clock
 *
 * Outputs:
 * - PricingResolution: includes unit price, extended price (unit * quantity), and which price type was applied
 *
 * Errors:
 * - IllegalArgumentException: invalid inputs (blank fields, quantity <= 0)
 * - NoSuchElementException: no active price configured for (store, sku, currency)
 *
 * Side effects:
 * - Reads from database via PriceRepository
 * - Logs start/end and notable decisions for debuggability
 */
@Service
public class PricingResolutionFlow {

  private static final Logger log = LoggerFactory.getLogger(PricingResolutionFlow.class);

  private final PriceRepository priceRepository;
  private final Clock clock;

  public PricingResolutionFlow(PriceRepository priceRepository, Clock clock) {
    this.priceRepository = priceRepository;
    this.clock = clock;
  }

  /**
   * PUBLIC_INTERFACE
   *
   * Resolve effective price for the given inputs.
   */
  public PricingResolution resolve(PricingResolutionRequest request) {
    /** Resolves the effective unit and extended price for a given SKU in a given store/currency. */
    validate(request);

    OffsetDateTime at = request.at() != null ? request.at() : OffsetDateTime.now(clock);

    log.info("PricingResolutionFlow.start store={} sku={} currency={} qty={} at={}",
        request.storeCode(), request.sku(), request.currencyCode(), request.quantity(), at);

    Price price = priceRepository
        .findByStoreCodeAndSkuAndCurrencyCodeAndActiveIsTrue(request.storeCode(), request.sku(), request.currencyCode())
        .orElseThrow(() -> new NoSuchElementException(
            "No active price found for store=" + request.storeCode() + " sku=" + request.sku() + " currency=" + request.currencyCode()
        ));

    AppliedPrice appliedPrice = determineAppliedPrice(price, at);

    BigDecimal unit = appliedPrice.unitPrice();
    BigDecimal extended = unit.multiply(BigDecimal.valueOf(request.quantity()))
        // normalize for currency presentation; keep 4dp storage but API returns 2dp by default
        .setScale(2, RoundingMode.HALF_UP);

    PricingResolution result = new PricingResolution(
        request.storeCode(),
        request.sku(),
        request.currencyCode(),
        request.quantity(),
        at,
        unit.setScale(2, RoundingMode.HALF_UP),
        extended,
        appliedPrice.priceType(),
        price.getRegularPrice() != null ? price.getRegularPrice().setScale(2, RoundingMode.HALF_UP) : null,
        price.getSalePrice() != null ? price.getSalePrice().setScale(2, RoundingMode.HALF_UP) : null,
        price.getSaleStartAt(),
        price.getSaleEndAt()
    );

    log.info("PricingResolutionFlow.end store={} sku={} currency={} qty={} type={} unit={} extended={}",
        request.storeCode(), request.sku(), request.currencyCode(), request.quantity(),
        result.appliedPriceType(), result.unitPrice(), result.extendedPrice());

    return result;
  }

  private void validate(PricingResolutionRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request must not be null");
    }
    if (isBlank(request.storeCode())) {
      throw new IllegalArgumentException("storeCode is required");
    }
    if (isBlank(request.sku())) {
      throw new IllegalArgumentException("sku is required");
    }
    if (isBlank(request.currencyCode()) || request.currencyCode().trim().length() != 3) {
      throw new IllegalArgumentException("currencyCode must be a 3-letter code");
    }
    if (request.quantity() <= 0) {
      throw new IllegalArgumentException("quantity must be > 0");
    }
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

  private static AppliedPrice determineAppliedPrice(Price price, OffsetDateTime at) {
    // Sale is considered only if salePrice exists AND the sale window contains "at"
    Optional<BigDecimal> sale = Optional.ofNullable(price.getSalePrice());
    if (sale.isPresent() && isWithinSaleWindow(price.getSaleStartAt(), price.getSaleEndAt(), at)) {
      return new AppliedPrice(PriceType.SALE, sale.get());
    }
    return new AppliedPrice(PriceType.REGULAR, price.getRegularPrice());
  }

  private static boolean isWithinSaleWindow(OffsetDateTime start, OffsetDateTime end, OffsetDateTime at) {
    if (at == null) return false;
    boolean afterStart = (start == null) || !at.isBefore(start);
    boolean beforeEnd = (end == null) || !at.isAfter(end);
    // If both bounds are null, treat it as always on sale when salePrice is present.
    return afterStart && beforeEnd;
  }

  private record AppliedPrice(PriceType priceType, BigDecimal unitPrice) {}

  public enum PriceType {
    REGULAR,
    SALE
  }
}
