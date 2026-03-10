package com.shopizer.payment.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Authorize-only payment response (Phase 1).")
public record AuthorizePaymentResponse(
    @Schema(description = "Internal payment intent id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa9")
    UUID paymentIntentId,

    @Schema(description = "Provider name", example = "PAYPAL")
    String provider,

    @Schema(description = "Payment intent status", example = "AUTHORIZED")
    String status,

    @Schema(description = "Provider authorization id (if available)", example = "3C679366HH908993F")
    String providerAuthorizationId,

    @Schema(description = "Provider order id (if available)", example = "5O190127TN364715T")
    String providerOrderId,

    @Schema(description = "Creation timestamp")
    OffsetDateTime createdAt
) {}
