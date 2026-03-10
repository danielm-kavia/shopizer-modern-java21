package com.shopizer.checkout.web;

import com.shopizer.checkout.service.CheckoutExceptions.CheckoutOrchestrationException;
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
