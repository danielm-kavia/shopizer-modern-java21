package com.shopizer.checkout.client.dto;

import java.math.BigDecimal;

/**
 * DTO for tax-service /api/tax/calculate response.
 */
public record TaxCalculateResponse(
    Long storeId,
    BigDecimal taxRate,
    BigDecimal taxableAmount,
    BigDecimal taxAmount
) {}
