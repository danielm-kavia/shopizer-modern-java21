package com.shopizer.cart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body to add quantity of a product to a cart.
 */
public record AddItemRequest(
    @Schema(description = "Product id", example = "4e7b6f4f-60e9-4c80-a270-cc5d0a0b6d0a")
    @NotNull UUID productId,

    @Schema(description = "Quantity to add", example = "2")
    @Min(1) int quantity
) {}
