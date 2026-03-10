package com.shopizer.shipping.flow;

import com.shopizer.shipping.provider.ShippingProviderRegistry;
import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestration flow for shipping rate quotes.
 *
 * Contract:
 * Inputs:
 *  - RateQuoteRequest: validated, normalized request (weights in grams, currency non-empty)
 * Outputs:
 *  - RateQuoteResult: requestId + quotedAt + list of quotes (possibly empty)
 * Errors:
 *  - IllegalArgumentException for invalid inputs (should be prevented by controller validation)
 * Side effects:
 *  - Delegates to providers (in-memory stub providers in Phase 1)
 *  - Optional logging/persistence can be added behind an adapter without changing controller.
 */
@Service
public class RateQuoteFlow {

  private static final Logger log = LoggerFactory.getLogger(RateQuoteFlow.class);

  private final ShippingProviderRegistry registry;
  private final Clock clock;

  public RateQuoteFlow(ShippingProviderRegistry registry) {
    this(registry, Clock.systemUTC());
  }

  public RateQuoteFlow(ShippingProviderRegistry registry, Clock clock) {
    this.registry = registry;
    this.clock = clock;
  }

  // PUBLIC_INTERFACE
  public RateQuoteResult getRateQuotes(RateQuoteRequest request) {
    /** Executes the rate-quote flow and returns quotes from all registered providers. */
    UUID requestId = UUID.randomUUID();
    Instant now = clock.instant();

    log.info("RateQuoteFlow.start requestId={} destCountry={} currency={} totalWeightGrams={}",
        requestId, request.destinationCountry(), request.currency(), request.totalWeightGrams());

    List<RateQuote> quotes = registry.quoteAll(request);

    log.info("RateQuoteFlow.end requestId={} quoteCount={}", requestId, quotes.size());
    return new RateQuoteResult(requestId, now, request.currency(), quotes);
  }
}
