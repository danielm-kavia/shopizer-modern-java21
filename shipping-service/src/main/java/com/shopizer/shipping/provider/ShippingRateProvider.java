package com.shopizer.shipping.provider;

import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import java.util.List;

/**
 * Provider SPI for shipping rate quotes.
 *
 * Implementations should be deterministic and return either:
 * - a list of quotes, or
 * - an empty list if they cannot service the request.
 */
public interface ShippingRateProvider {

  /**
   * Provider identifier (stable).
   */
  String providerCode();

  /**
   * Returns quotes for a request, or an empty list if unsupported.
   */
  List<RateQuote> quote(RateQuoteRequest request);
}
