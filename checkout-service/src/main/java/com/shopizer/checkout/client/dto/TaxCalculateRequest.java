package com.shopizer.checkout.client.dto;

import java.util.List;

/**
 * DTO for tax-service /api/tax/calculate request.
 *
 * <p>Contract:
 * - storeId required
 * - lines required (non-empty for meaningful result)
 */
public record TaxCalculateRequest(
    Long storeId,
    List<TaxLine> lines
) {}
