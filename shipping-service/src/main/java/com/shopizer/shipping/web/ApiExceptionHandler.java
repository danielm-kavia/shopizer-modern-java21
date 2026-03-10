package com.shopizer.shipping.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Central exception mapping for REST endpoints.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
    pd.setTitle("Bad Request");
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("path", request.getRequestURI());
    pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
        .map(this::fieldErrorToMap)
        .toList());
    return pd;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setTitle("Bad Request");
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("path", request.getRequestURI());
    return pd;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled error path={} error={}", request.getRequestURI(), ex.toString(), ex);
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    pd.setTitle("Internal Server Error");
    pd.setProperty("timestamp", Instant.now().toString());
    pd.setProperty("path", request.getRequestURI());
    return pd;
  }

  private Map<String, Object> fieldErrorToMap(FieldError fe) {
    return Map.of(
        "field", fe.getField(),
        "message", fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage()
    );
  }
}
