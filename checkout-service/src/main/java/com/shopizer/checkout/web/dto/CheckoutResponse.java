package com.shopizer.checkout.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Response payload for checkout creation.
 */
public record CheckoutResponse(
    @Schema(description = "Created order id") UUID orderId,
    @Schema(description = "Checkout status") String status,

    @Schema(description = "Computed subtotal (sum of extended prices before discount/tax).", example = "100.00")
    java.math.BigDecimal subtotal,

    @Schema(description = "Discount applied to subtotal via promotions-service (0 when no coupon).", example = "10.00")
    java.math.BigDecimal discount,

    @Schema(description = "Computed tax amount.", example = "8.25")
    java.math.BigDecimal tax,

    @Schema(description = "Computed total after discount + tax.", example = "98.25")
    java.math.BigDecimal total,

    @Schema(description = "Payment intent id created/used by payment-service for this checkout authorization")
    UUID paymentIntentId,

    @Schema(description = "Payment provider (e.g. PAYPAL)")
    String paymentProvider,

    @Schema(description = "Payment authorization status returned by payment-service (e.g. AUTHORIZED, FAILED)")
    String paymentStatus,

    @Schema(description = "Provider authorization id (if available)")
    String providerAuthorizationId,

    @Schema(description = "Provider order id (if available)")
    String providerOrderId
) {}
