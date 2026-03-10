package com.shopizer.checkout.client.dto;

import java.util.UUID;

/**
 * Minimal cart item representation used by checkout-service when reading carts.
 */
public record CartItemResponse(UUID productId, int quantity) {}
