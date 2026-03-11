package com.shopizer.checkout.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO for payment-service authorize endpoint.
 *
 * <p>Must remain wire-compatible with payment-service
 * {@code com.shopizer.payment.web.dto.AuthorizePaymentRequest}.
 */
@Schema(description = "Authorize-only payment request (Phase 1).")
public record AuthorizePaymentRequest(

    @NotNull
    @Schema(description = "Merchant store id", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID merchantStoreId,

    @NotNull
    @Schema(description = "Order id to authorize", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID orderId,

    @Schema(description = "Customer id (optional, for traceability)")
    UUID customerId,

    @NotBlank
    @Schema(description = "ISO-4217 currency code", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    String currency,

    @Min(0)
    @Schema(description = "Amount in minor currency units (e.g. cents)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2599")
    long amountMinor,

    @NotBlank
    @Schema(description = "Idempotency key for safe retries", requiredMode = Schema.RequiredMode.REQUIRED)
    String idempotencyKey
) {}
