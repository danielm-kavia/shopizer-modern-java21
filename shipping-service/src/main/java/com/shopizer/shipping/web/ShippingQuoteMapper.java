package com.shopizer.shipping.web;

import com.shopizer.shipping.provider.model.RateQuote;
import com.shopizer.shipping.provider.model.RateQuoteRequest;
import com.shopizer.shipping.web.dto.QuoteRequest;
import com.shopizer.shipping.web.dto.QuoteResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Maps between API DTOs and internal domain/provider models.
 */
@Component
public class ShippingQuoteMapper {

  // PUBLIC_INTERFACE
  public RateQuoteRequest toProviderRequest(QuoteRequest api) {
    /** Normalizes the API quote request into a provider request (aggregated total weight). */
    int totalWeightGrams = api.items().stream()
        .mapToInt(i -> i.weightGrams() * i.quantity())
        .sum();

    return new RateQuoteRequest(
        api.destination().country(),
        api.destination().postalCode(),
        api.currency(),
        totalWeightGrams
    );
  }

  // PUBLIC_INTERFACE
  public QuoteResponse toApiResponse(java.util.UUID requestId, java.time.Instant quotedAt, String currency, List<RateQuote> quotes) {
    /** Maps provider quotes to API response payload. */
    return new QuoteResponse(
        requestId,
        quotedAt,
        currency,
        quotes.stream()
            .map(q -> new QuoteResponse.Quote(q.provider(), q.serviceLevel(), q.serviceName(), q.amount()))
            .toList()
    );
  }
}
