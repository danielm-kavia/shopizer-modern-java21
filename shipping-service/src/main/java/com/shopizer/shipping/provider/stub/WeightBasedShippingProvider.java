package com.shopizer.shipping.provider.stub;

import com.shopizer.shipping.provider.ShippingRateProvider;
import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Stub provider: price is a base fee + per-kg fee using totalWeightGrams.
 *
 * This is intentionally simple and deterministic for Phase 1.
 */
@Component
public class WeightBasedShippingProvider implements ShippingRateProvider {

  @Override
  public String providerCode() {
    return "STUB_WEIGHT";
  }

  @Override
  public List<RateQuote> quote(RateQuoteRequest request) {
    BigDecimal base = new BigDecimal("4.50");
    BigDecimal perKg = new BigDecimal("2.25");

    BigDecimal kg = BigDecimal.valueOf(request.totalWeightGrams()).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    BigDecimal amount = base.add(perKg.multiply(kg)).setScale(2, RoundingMode.HALF_UP);

    return List.of(new RateQuote(
        providerCode(),
        "ECONOMY",
        "Economy Shipping (by weight)",
        amount
    ));
  }
}
