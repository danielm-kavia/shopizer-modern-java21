package com.shopizer.shipping.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Contract for a shipping rate quote response.
 *
 * Invariants:
 * - requestId is always present for traceability
 * - quotes may be empty if no provider can service the request
 */
@Schema(name = "QuoteResponse", description = "Response payload containing available shipping rate quotes")
public record QuoteResponse(
    @Schema(description = "Server-generated request id for tracing/debugging", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID requestId,

    @Schema(description = "Timestamp when the quote was computed", requiredMode = Schema.RequiredMode.REQUIRED)
    Instant quotedAt,

    @Schema(description = "Currency matching request currency", example = "USD")
    String currency,

    @Schema(description = "List of available quotes (may be empty)")
    List<Quote> quotes
) {

  @Schema(name = "Quote", description = "A single shipping quote option")
  public record Quote(
      @Schema(description = "Provider code", example = "STUB_FLAT_RATE")
      String provider,

      @Schema(description = "Service level identifier", example = "STANDARD")
      String serviceLevel,

      @Schema(description = "Human-friendly name", example = "Standard Shipping")
      String serviceName,

      @Schema(description = "Quoted shipping amount", example = "9.99")
      BigDecimal amount
  ) {}
}
