package com.shopizer.inventory.web;

import com.shopizer.inventory.service.InventoryExceptions.InsufficientAvailable;
import com.shopizer.inventory.service.InventoryExceptions.InvalidQuantity;
import com.shopizer.inventory.service.InventoryExceptions.NotFound;
import com.shopizer.inventory.web.dto.InventoryDtos.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps exceptions to consistent HTTP error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NotFound.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(NotFound ex) {
    log.info("inventory-service not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse("NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(InvalidQuantity.class)
  public ResponseEntity<ApiErrorResponse> handleInvalid(InvalidQuantity ex) {
    log.info("inventory-service invalid request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse("INVALID_REQUEST", ex.getMessage()));
  }

  @ExceptionHandler(InsufficientAvailable.class)
  public ResponseEntity<ApiErrorResponse> handleInsufficient(InsufficientAvailable ex) {
    log.info("inventory-service conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiErrorResponse("INSUFFICIENT_AVAILABLE", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
    log.error("inventory-service unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse("INTERNAL_ERROR", "Unexpected server error"));
  }
}
