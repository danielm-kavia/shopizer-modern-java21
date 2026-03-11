package com.shopizer.tax.web;

import com.shopizer.tax.service.TaxExceptions;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handling for tax-service REST API.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @Schema(name = "ApiErrorResponse", description = "Standard API error response.")
  public record ApiErrorResponse(
      @Schema(description = "Timestamp when the error occurred.") OffsetDateTime timestamp,
      @Schema(description = "HTTP status code.") int status,
      @Schema(description = "Error message.") String message,
      @Schema(description = "Request path.") String path
  ) {}

  @ExceptionHandler(TaxExceptions.StoreTaxRateNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleRateNotFound(
      TaxExceptions.StoreTaxRateNotFoundException ex,
      HttpServletRequest request
  ) {
    ApiErrorResponse body = new ApiErrorResponse(
        OffsetDateTime.now(),
        HttpStatus.NOT_FOUND.value(),
        ex.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }
}
