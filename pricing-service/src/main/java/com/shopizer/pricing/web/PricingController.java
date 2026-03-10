package com.shopizer.pricing.web;

import com.shopizer.pricing.service.PricingResolutionFlow;
import com.shopizer.pricing.service.PricingResolutionModels.PricingResolution;
import com.shopizer.pricing.service.PricingResolutionModels.PricingResolutionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * Pricing read-only API boundary.
 *
 * Controllers must delegate to PricingResolutionFlow (single canonical behavior path).
 */
@RestController
@RequestMapping("/api/pricing")
@Tag(name = "Pricing")
public class PricingController {

  private final PricingResolutionFlow pricingResolutionFlow;

  public PricingController(PricingResolutionFlow pricingResolutionFlow) {
    this.pricingResolutionFlow = pricingResolutionFlow;
  }

  // PUBLIC_INTERFACE
  @GetMapping("/resolve")
  @Operation(
      summary = "Resolve effective price for a SKU",
      description = "Resolves the effective unit price (regular vs sale) and extended price for a given store + SKU + currency and quantity. "
          + "Sale price is applied only when configured and the evaluation time is within the sale window."
  )
  public PricingResolution resolve(
      @Parameter(description = "Store code (tenant/store identifier)", required = true, example = "DEFAULT")
      @RequestParam("storeCode")
      @NotBlank String storeCode,

      @Parameter(description = "Product SKU", required = true, example = "SKU-123")
      @RequestParam("sku")
      @NotBlank String sku,

      @Parameter(description = "Currency code (ISO-4217)", required = true, example = "USD")
      @RequestParam("currency")
      @NotBlank
      @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must be a 3-letter code")
      String currency,

      @Parameter(description = "Quantity (must be > 0)", required = true, example = "2")
      @RequestParam("qty")
      @Min(1) int qty,

      @Parameter(
          description = "Optional evaluation timestamp (defaults to now). ISO-8601 offset datetime.",
          schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T10:00:00Z")
      )
      @RequestParam(value = "at", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
      OffsetDateTime at
  ) {
    /** Resolve effective unit and extended price for store/SKU/currency. */
    PricingResolutionRequest req = new PricingResolutionRequest(
        storeCode.trim(),
        sku.trim(),
        currency.trim().toUpperCase(),
        qty,
        at
    );
    return pricingResolutionFlow.resolve(req);
  }
}
