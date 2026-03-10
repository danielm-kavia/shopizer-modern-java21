package com.shopizer.shipping.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Contract for a shipping rate quote request.
 *
 * Invariants:
 * - currency must be a 3-letter code (e.g. "USD")
 * - destination.country must be ISO-3166-1 alpha-2 (best-effort validation: non-blank)
 * - total weight must be positive; item weights are specified in grams
 */
@Schema(name = "QuoteRequest", description = "Request payload to obtain shipping rate quotes")
public record QuoteRequest(
    @NotNull @Valid @Schema(description = "Destination address info", requiredMode = Schema.RequiredMode.REQUIRED)
    Destination destination,

    @NotBlank @Schema(description = "ISO 4217 currency code for returned amounts (e.g. USD)", example = "USD")
    String currency,

    @NotEmpty @Valid @Schema(description = "Items being shipped", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Item> items
) {

  @Schema(name = "Destination", description = "Destination details used for rating")
  public record Destination(
      @NotBlank @Schema(description = "ISO-3166-1 alpha-2 country code", example = "US")
      String country,

      @Schema(description = "Postal code (if applicable)", example = "94107")
      String postalCode,

      @Schema(description = "State/region (if applicable)", example = "CA")
      String region
  ) {}

  @Schema(name = "Item", description = "A shippable item (Phase 1 uses weights only)")
  public record Item(
      @NotBlank @Schema(description = "Client-provided item id/sku", example = "SKU-123")
      String sku,

      @Min(1) @Schema(description = "Quantity", example = "2")
      int quantity,

      @Min(1) @Schema(description = "Weight per unit in grams", example = "500")
      int weightGrams
  ) {}
}
