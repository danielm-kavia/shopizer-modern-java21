package com.shopizer.shipping.provider.stub;

import com.shopizer.shipping.provider.ShippingRateProvider;
import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Stub provider: always offers a flat rate regardless of destination/weight.
 */
@Component
public class FlatRateShippingProvider implements ShippingRateProvider {

  @Override
  public String providerCode() {
    return "STUB_FLAT_RATE";
  }

  @Override
  public List<RateQuote> quote(RateQuoteRequest request) {
    // Always available; deterministic.
    return List.of(new RateQuote(
        providerCode(),
        "STANDARD",
        "Standard Shipping",
        new BigDecimal("9.99")
    ));
  }
}
