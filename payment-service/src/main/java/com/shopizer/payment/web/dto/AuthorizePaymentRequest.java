package com.shopizer.payment.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "Authorize-only payment request (Phase 1).")
public record AuthorizePaymentRequest(

    @NotNull
    @Schema(description = "Merchant store id", requiredMode = Schema.RequiredMode.REQUIRED, example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    UUID merchantStoreId,

    @NotNull
    @Schema(description = "Order id to authorize", requiredMode = Schema.RequiredMode.REQUIRED, example = "3fa85f64-5717-4562-b3fc-2c963f66afa7")
    UUID orderId,

    @Schema(description = "Customer id (optional, for traceability)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa8")
    UUID customerId,

    @NotBlank
    @Schema(description = "ISO-4217 currency code", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    String currency,

    @Min(0)
    @Schema(description = "Amount in minor currency units (e.g. cents)", requiredMode = Schema.RequiredMode.REQUIRED, example = "2599")
    long amountMinor,

    @NotBlank
    @Schema(description = "Idempotency key. Same key should return same authorization result for safe retries.",
        requiredMode = Schema.RequiredMode.REQUIRED, example = "checkout-9d65f9cf-2a3d-4b50-9ea2-7e3c2b1f1d8c")
    String idempotencyKey
) {}
