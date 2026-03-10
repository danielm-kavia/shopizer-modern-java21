package com.shopizer.checkout.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload to create an order from a cart.
 *
 * Contract:
 * - cartId must exist and include at least one item.
 * - merchantStoreId/customerId are required and must match the cart's values (validated by checkout-service).
 */
public record CreateCheckoutRequest(
    @Schema(description = "Cart ID to checkout", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID cartId,

    @Schema(description = "Merchant store ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID merchantStoreId,

    @Schema(description = "Customer ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull UUID customerId
) {}
