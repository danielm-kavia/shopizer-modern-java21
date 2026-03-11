package com.shopizer.checkout.web;

import com.shopizer.checkout.service.CreateOrderFromCartFlow;
import com.shopizer.checkout.web.dto.CheckoutResponse;
import com.shopizer.checkout.web.dto.CreateCheckoutRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Checkout REST API.
 *
 * Phase 1: create order from cart items only (no payment/shipping).
 *
 * Security:
 * - Requires Authorization: Bearer <jwt>
 * - The bearer token is propagated to downstream cart-service and order-service.
 */
@RestController
@RequestMapping("/api/checkout")
@Tag(name = "Checkout", description = "Checkout orchestration endpoints (Phase 1)")
public class CheckoutController {

  private final CreateOrderFromCartFlow createOrderFromCartFlow;

  public CheckoutController(CreateOrderFromCartFlow createOrderFromCartFlow) {
    this.createOrderFromCartFlow = createOrderFromCartFlow;
  }

  // PUBLIC_INTERFACE
  @PostMapping("/orders")
  @Operation(
      operationId = "checkoutCreateOrderFromCart",
      summary = "Create an order from a cart",
      description = "Fetches the cart from cart-service, validates ownership/match, then creates an order in order-service from cart items.",
      responses = {
          @ApiResponse(responseCode = "201", description = "Order created",
              content = @Content(schema = @Schema(implementation = CheckoutResponse.class))),
          @ApiResponse(responseCode = "400", description = "Validation error"),
          @ApiResponse(responseCode = "401", description = "Unauthorized")
      }
  )
  public ResponseEntity<CheckoutResponse> createOrderFromCart(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
      @Valid @RequestBody CreateCheckoutRequest request
  ) {
    /** Entry boundary for the CreateOrderFromCartFlow. */
    var result = createOrderFromCartFlow.execute(
        request.cartId(),
        request.merchantStoreId(),
        request.customerId(),
        request.storeCode(),
        request.couponCode(),
        authorization
    );

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new CheckoutResponse(
            result.orderId(),
            "CREATED",
            result.subtotal(),
            result.discount(),
            result.tax(),
            result.total()
        ));
  }
}
