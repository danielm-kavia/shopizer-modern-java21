package com.shopizer.cart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request body for "get or create active cart".
 */
public record CreateOrGetCartRequest(
    @Schema(description = "Merchant store id", example = "b3b7dfc8-9d66-4e1c-9dc1-4d7fb8a5dca7")
    @NotNull UUID merchantStoreId,

    @Schema(description = "Customer id", example = "a5d6a3a5-1c37-47b1-8c41-6f019f43d0cb")
    @NotNull UUID customerId,

    @Schema(description = "ISO 4217 currency code", example = "USD")
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code (e.g., USD)")
    String currency
) {}
