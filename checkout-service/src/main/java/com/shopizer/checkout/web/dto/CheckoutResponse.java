package com.shopizer.checkout.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response payload for checkout creation.
 */
public record CheckoutResponse(
    @Schema(description = "Created order id") UUID orderId,
    @Schema(description = "Checkout status") String status
) {}
