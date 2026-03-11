package com.shopizer.promotions.web;

import com.shopizer.promotions.service.PromotionsExceptions;
import com.shopizer.promotions.web.dto.PromotionsDtos.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Maps domain/service exceptions to stable HTTP error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(PromotionsExceptions.CouponNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(
      PromotionsExceptions.CouponNotFoundException ex,
      HttpServletRequest request) {

    log.warn("promotions.error code=COUPON_NOT_FOUND path={} msg={}", request.getRequestURI(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse("COUPON_NOT_FOUND", ex.getMessage()));
  }

  @ExceptionHandler(PromotionsExceptions.CouponInactiveException.class)
  public ResponseEntity<ApiErrorResponse> handleInactive(
      PromotionsExceptions.CouponInactiveException ex,
      HttpServletRequest request) {

    log.warn("promotions.error code=COUPON_INACTIVE path={} msg={}", request.getRequestURI(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ApiErrorResponse("COUPON_INACTIVE", ex.getMessage()));
  }

  @ExceptionHandler(PromotionsExceptions.CurrencyMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleCurrencyMismatch(
      PromotionsExceptions.CurrencyMismatchException ex,
      HttpServletRequest request) {

    log.warn("promotions.error code=CURRENCY_MISMATCH path={} msg={}", request.getRequestURI(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ApiErrorResponse("CURRENCY_MISMATCH", ex.getMessage()));
  }

  @ExceptionHandler(PromotionsExceptions.InvalidApplyRequestException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalid(
      PromotionsExceptions.InvalidApplyRequestException ex,
      HttpServletRequest request) {

    log.warn("promotions.error code=INVALID_REQUEST path={} msg={}", request.getRequestURI(), ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse("INVALID_REQUEST", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("promotions.error code=UNEXPECTED path={} msg={}", request.getRequestURI(), ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse("UNEXPECTED", "Unexpected error"));
  }
}
