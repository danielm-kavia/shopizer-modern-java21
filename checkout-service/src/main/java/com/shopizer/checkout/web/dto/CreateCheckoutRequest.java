package com.shopizer.checkout.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload to create an order from a cart.
 *
 * Contract:
 * - cartId must exist and include at least one item.
 * - merchantStoreId/customerId are required and must match the cart's values (validated by checkout-service).
 * - destination is required for shipping quotes.
 * - selectedShippingQuote is optional; if provided it must match one of the returned quotes.
 */
public record CreateCheckoutRequest(
    @Schema(description = "Cart ID to checkout", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID cartId,

    @Schema(description = "Merchant store ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID merchantStoreId,

    @Schema(description = "Customer ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID customerId,

    @Schema(description = "Store code for pricing resolution (tenant/store identifier). If omitted, defaults to DEFAULT.", example = "DEFAULT")
    String storeCode,

    @Schema(description = "Optional coupon code to apply during checkout.", example = "SAVE10")
    String couponCode,

    @Schema(
        description = "UI-selected payment method (placeholder). Phase 1 currently authorizes PayPal regardless; other values are accepted for forward-compatibility.",
        example = "paypal"
    )
    String paymentMethod,

    @NotNull @Valid
    @Schema(description = "Shipping destination used for obtaining rate quotes", requiredMode = Schema.RequiredMode.REQUIRED)
    Destination destination,

    @Valid
    @Schema(description = "Optional shipping quote selection (provider+serviceLevel). If omitted, the cheapest quote is selected when available.")
    SelectedShippingQuote selectedShippingQuote,

    @Min(1)
    @Schema(description = "Default weight per unit in grams for items when cart items do not carry weights (Phase 1 defaulting).", example = "500")
    Integer defaultItemWeightGrams
) {

  @Schema(name = "Destination", description = "Destination used for shipping quote rating")
  public record Destination(
      @NotBlank @Schema(description = "ISO-3166-1 alpha-2 country code", example = "US")
      String country,

      @Schema(description = "Postal code", example = "94107")
      String postalCode,

      @Schema(description = "State/region", example = "CA")
      String region
  ) {}

  @Schema(name = "SelectedShippingQuote", description = "Client-selected quote identity")
  public record SelectedShippingQuote(
      @NotBlank @Schema(description = "Provider code", example = "STUB_FLAT_RATE")
      String provider,

      @NotBlank @Schema(description = "Service level identifier", example = "STANDARD")
      String serviceLevel
  ) {}
}
