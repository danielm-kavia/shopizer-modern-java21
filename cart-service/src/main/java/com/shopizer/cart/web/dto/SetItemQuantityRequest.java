package com.shopizer.cart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body to set quantity for a product in a cart (0 removes).
 */
public record SetItemQuantityRequest(
    @Schema(description = "Product id", example = "4e7b6f4f-60e9-4c80-a270-cc5d0a0b6d0a")
    @NotNull UUID productId,

    @Schema(description = "Quantity to set (0 removes item)", example = "1")
    @Min(0) int quantity
) {}
