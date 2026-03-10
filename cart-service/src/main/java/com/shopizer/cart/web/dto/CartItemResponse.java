package com.shopizer.cart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * API response view of a cart item.
 */
public record CartItemResponse(
    @Schema(description = "Cart item id") UUID id,
    @Schema(description = "Product id") UUID productId,
    @Schema(description = "Quantity") int quantity,
    @Schema(description = "Created timestamp") Instant createdAt,
    @Schema(description = "Updated timestamp") Instant updatedAt
) {}
