package com.shopizer.shipping.flow;

import com.shopizer.shipping.provider.model.RateQuote;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of the RateQuoteFlow execution.
 */
public record RateQuoteResult(
    UUID requestId,
    Instant quotedAt,
    String currency,
    List<RateQuote> quotes
) {}
