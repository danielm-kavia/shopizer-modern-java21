package com.shopizer.shipping.web;

import com.shopizer.shipping.flow.RateQuoteFlow;
import com.shopizer.shipping.flow.RateQuoteResult;
import com.shopizer.shipping.web.dto.QuoteRequest;
import com.shopizer.shipping.web.dto.QuoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * REST boundary for shipping quote APIs.
 *
 * This controller performs input validation and delegates all logic to RateQuoteFlow.
 */
@RestController
@RequestMapping(path = "/api/shipping", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Shipping", description = "Shipping rate quote endpoints")
public class ShippingQuoteController {

  private final RateQuoteFlow flow;
  private final ShippingQuoteMapper mapper;

  public ShippingQuoteController(RateQuoteFlow flow, ShippingQuoteMapper mapper) {
    this.flow = flow;
    this.mapper = mapper;
  }

  // PUBLIC_INTERFACE
  @PostMapping(path = "/quotes", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get shipping rate quotes",
      description = "Returns shipping rate quote options using stub in-memory providers (Phase 1)."
  )
  public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
    /** Creates a rate quote request and returns a list of available quotes. */
    RateQuoteResult result = flow.getRateQuotes(mapper.toProviderRequest(request));
    return mapper.toApiResponse(result.requestId(), result.quotedAt(), result.currency(), result.quotes());
  }
}
