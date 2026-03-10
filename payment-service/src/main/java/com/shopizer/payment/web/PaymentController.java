package com.shopizer.payment.web;

import com.shopizer.payment.service.AuthorizationFlow;
import com.shopizer.payment.service.model.AuthorizationAttempt;
import com.shopizer.payment.web.dto.ApiErrorResponse;
import com.shopizer.payment.web.dto.AuthorizePaymentRequest;
import com.shopizer.payment.web.dto.AuthorizePaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Payment endpoints (Phase 1: authorize-only).
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Authorize-only payment endpoints")
public class PaymentController {

  private final AuthorizationFlow authorizationFlow;

  public PaymentController(AuthorizationFlow authorizationFlow) {
    this.authorizationFlow = authorizationFlow;
  }

  // PUBLIC_INTERFACE
  @PostMapping(value = "/paypal/authorize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      operationId = "authorizePayPal",
      summary = "Authorize a payment using PayPal (authorize-only)",
      description = """
          Creates (or reuses via idempotency key) a PaymentIntent and performs a PayPal authorization.
          Phase 1: authorize only (no capture/refund yet).
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "Authorization created or returned (idempotent retry)"),
          @ApiResponse(responseCode = "400", description = "Validation error",
              content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
          @ApiResponse(responseCode = "502", description = "Provider error",
              content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
          @ApiResponse(responseCode = "503", description = "Missing provider configuration",
              content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
      }
  )
  public AuthorizePaymentResponse authorizePayPal(@Valid @RequestBody AuthorizePaymentRequest req) {
    AuthorizationAttempt attempt = new AuthorizationAttempt(
        req.merchantStoreId(),
        req.orderId(),
        req.customerId(),
        req.currency(),
        req.amountMinor(),
        req.idempotencyKey()
    );

    AuthorizationFlow.AuthorizationOutcome outcome = authorizationFlow.authorizePayPal(attempt);

    return new AuthorizePaymentResponse(
        outcome.paymentIntentId(),
        outcome.provider(),
        outcome.status(),
        outcome.providerAuthorizationId(),
        outcome.providerOrderId(),
        outcome.createdAt()
    );
  }
}
