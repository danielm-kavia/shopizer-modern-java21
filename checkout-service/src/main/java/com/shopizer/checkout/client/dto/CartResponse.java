package com.shopizer.checkout.client.dto;

import java.util.List;
import java.util.UUID;

/**
 * Minimal cart representation used by checkout-service.
 *
 * Invariants relied upon:
 * - id, merchantStoreId, customerId are present
 * - currency is a 3-letter code
 * - items list is present (possibly empty)
 */
public record CartResponse(
    UUID id,
    UUID merchantStoreId,
    UUID customerId,
    String currency,
    List<CartItemResponse> items
) {}
