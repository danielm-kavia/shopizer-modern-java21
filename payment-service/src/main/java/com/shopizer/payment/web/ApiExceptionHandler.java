package com.shopizer.payment.web;

import com.shopizer.payment.service.PaymentExceptions;
import com.shopizer.payment.web.dto.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps internal exceptions to stable HTTP responses for API clients.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(PaymentExceptions.ValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(PaymentExceptions.ValidationException e) {
    return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_ERROR", e.getMessage()));
  }

  @ExceptionHandler(PaymentExceptions.NotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(PaymentExceptions.NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(PaymentExceptions.ConfigurationException.class)
  public ResponseEntity<ApiErrorResponse> handleConfig(PaymentExceptions.ConfigurationException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ApiErrorResponse("PAYMENT_CONFIGURATION_ERROR", e.getMessage()));
  }

  @ExceptionHandler(PaymentExceptions.ProviderException.class)
  public ResponseEntity<ApiErrorResponse> handleProvider(PaymentExceptions.ProviderException e) {
    log.warn("payment.provider.error msg={}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiErrorResponse("PAYMENT_PROVIDER_ERROR", e.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception e) {
    log.error("payment.unhandled.error msg={}", e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse("INTERNAL_ERROR", "Unexpected error"));
  }
}
