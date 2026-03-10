package com.shopizer.cart.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * REST exception mapping.
 *
 * Boundary responsibility: map internal exceptions into stable HTTP error shapes.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNotFound(NoSuchElementException ex) {
    log.info("CartApi.notFound msg={}", ex.getMessage());
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    pd.setTitle("Not Found");
    pd.setDetail(ex.getMessage());
    return pd;
  }

  @ExceptionHandler(ErrorResponseException.class)
  public ProblemDetail handleErrorResponse(ErrorResponseException ex) {
    log.warn("CartApi.error status={} msg={}", ex.getStatusCode(), ex.getMessage());
    return ex.getBody();
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    log.error("CartApi.unhandledError type={} msg={}", ex.getClass().getName(), ex.getMessage(), ex);
    ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setTitle("Internal Server Error");
    pd.setDetail("Unexpected error");
    return pd;
  }
}
