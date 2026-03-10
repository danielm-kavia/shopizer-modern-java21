package com.shopizer.payment.service.model;

import java.util.UUID;

/**
 * Input model for AuthorizationFlow.
 *
 * Invariants are validated at the API boundary:
 * - merchantStoreId, orderId, currency, idempotencyKey non-null/non-blank
 * - amountMinor >= 0
 */
public record AuthorizationAttempt(
    UUID merchantStoreId,
    UUID orderId,
    UUID customerId,
    String currency,
    long amountMinor,
    String idempotencyKey
) {}
