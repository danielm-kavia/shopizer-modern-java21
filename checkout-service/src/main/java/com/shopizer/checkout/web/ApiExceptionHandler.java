package com.shopizer.checkout.web;

import com.shopizer.checkout.service.CheckoutExceptions.CheckoutInventoryReservationException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutOrchestrationException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutShippingQuoteException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutShippingSelectionException;
import com.shopizer.checkout.service.CheckoutExceptions.CheckoutValidationException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps exceptions to stable HTTP responses for checkout-service.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(CheckoutValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(CheckoutValidationException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "CHECKOUT_VALIDATION_ERROR",
        "message", ex.getMessage()
    ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "REQUEST_VALIDATION_ERROR",
        "message", "Request validation failed"
    ));
  }

  @ExceptionHandler(CheckoutInventoryReservationException.class)
  public ResponseEntity<Map<String, Object>> handleInventoryReservation(CheckoutInventoryReservationException ex) {
    // Phase 2: treat as conflict (cart cannot be checked out due to insufficient stock).
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "INVENTORY_RESERVATION_FAILED",
        "message", ex.getMessage()
    ));
  }

  @ExceptionHandler(CheckoutShippingSelectionException.class)
  public ResponseEntity<Map<String, Object>> handleShippingSelection(CheckoutShippingSelectionException ex) {
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "SHIPPING_SELECTION_INVALID",
        "message", ex.getMessage()
    ));
  }

  @ExceptionHandler(CheckoutShippingQuoteException.class)
  public ResponseEntity<Map<String, Object>> handleShippingQuote(CheckoutShippingQuoteException ex) {
    // Treat quote failures as a dependency error (caller can retry).
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "SHIPPING_QUOTE_FAILED",
        "message", ex.getMessage()
    ));
  }

  @ExceptionHandler(CheckoutOrchestrationException.class)
  public ResponseEntity<Map<String, Object>> handleOrchestration(CheckoutOrchestrationException ex) {
    // Phase 1: surface as 502 to indicate downstream dependency failure.
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "CHECKOUT_ORCHESTRATION_FAILED",
        "message", ex.getMessage()
    ));
  }
}
