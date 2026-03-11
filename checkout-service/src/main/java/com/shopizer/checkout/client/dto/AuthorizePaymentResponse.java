package com.shopizer.checkout.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for payment-service authorize endpoint.
 *
 * <p>Must remain wire-compatible with payment-service
 * {@code com.shopizer.payment.web.dto.AuthorizePaymentResponse}.
 */
@Schema(description = "Authorize-only payment response (Phase 1).")
public record AuthorizePaymentResponse(
    @Schema(description = "Internal payment intent id")
    UUID paymentIntentId,

    @Schema(description = "Provider name", example = "PAYPAL")
    String provider,

    @Schema(description = "Payment intent status", example = "AUTHORIZED")
    String status,

    @Schema(description = "Provider authorization id (if available)")
    String providerAuthorizationId,

    @Schema(description = "Provider order id (if available)")
    String providerOrderId,

    @Schema(description = "Creation timestamp")
    OffsetDateTime createdAt
) {}
