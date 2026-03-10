package com.shopizer.payment.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API error response payload.")
public record ApiErrorResponse(
    @Schema(description = "Machine-readable error code", example = "PAYMENT_PROVIDER_ERROR")
    String code,
    @Schema(description = "Human-readable error message", example = "PayPal authorization failed")
    String message
) {}
