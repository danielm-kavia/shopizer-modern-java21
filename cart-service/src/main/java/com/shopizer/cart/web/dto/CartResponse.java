package com.shopizer.cart.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * API response view of a cart with items.
 */
public record CartResponse(
    @Schema(description = "Cart id") UUID id,
    @Schema(description = "Merchant store id") UUID merchantStoreId,
    @Schema(description = "Customer id") UUID customerId,
    @Schema(description = "Currency code") String currency,
    @Schema(description = "Cart status") String status,
    @Schema(description = "Created timestamp") Instant createdAt,
    @Schema(description = "Updated timestamp") Instant updatedAt,
    @Schema(description = "Cart items") List<CartItemResponse> items
) {}
