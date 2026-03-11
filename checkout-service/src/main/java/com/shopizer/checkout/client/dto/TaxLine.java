package com.shopizer.checkout.client.dto;

import java.math.BigDecimal;

/**
 * DTO for tax-service line items.
 *
 * <p>Contract:
 * - sku required (Phase 1 not used for computation, but required by API contract).
 * - quantity >= 0
 * - unitPrice >= 0
 */
public record TaxLine(
    String sku,
    Integer quantity,
    BigDecimal unitPrice
) {}
