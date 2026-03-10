package com.shopizer.shipping.provider;

import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Central registry/dispatcher for ShippingRateProvider implementations.
 *
 * This is the single place where "which providers participate" is decided,
 * preventing scattered conditional logic across controllers/services.
 */
@Component
public class ShippingProviderRegistry {

  private static final Logger log = LoggerFactory.getLogger(ShippingProviderRegistry.class);

  private final List<ShippingRateProvider> providers;

  public ShippingProviderRegistry(List<ShippingRateProvider> providers) {
    this.providers = List.copyOf(providers);
    log.info("ShippingProviderRegistry initialized providers={}", this.providers.stream().map(ShippingRateProvider::providerCode).toList());
  }

  // PUBLIC_INTERFACE
  public List<RateQuote> quoteAll(RateQuoteRequest request) {
    /** Returns the union of quotes from all providers, sorted by amount ascending. */
    List<RateQuote> all = new ArrayList<>();
    for (ShippingRateProvider p : providers) {
      try {
        List<RateQuote> quotes = p.quote(request);
        if (quotes != null && !quotes.isEmpty()) {
          all.addAll(quotes);
        }
      } catch (RuntimeException ex) {
        // Provider failures are isolated and logged; flow continues with other providers.
        // This is explicit and observable, not a silent fallback.
        log.warn("Provider quote failed provider={} requestCountry={} currency={} weightGrams={} error={}",
            p.providerCode(), request.destinationCountry(), request.currency(), request.totalWeightGrams(), ex.toString(), ex);
      }
    }
    all.sort(Comparator.comparing(RateQuote::amount));
    return all;
  }
}
