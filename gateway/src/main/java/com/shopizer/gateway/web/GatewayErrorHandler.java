package com.shopizer.gateway.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gateway error handler to avoid opaque HTTP 500 responses when proxying fails.
 *
 * <p>Why this exists:
 * <ul>
 *   <li>Spring Cloud Gateway proxy failures (e.g., UnknownHost/Connect refused) often appear as generic 500s.</li>
 *   <li>This handler logs the full exception (stacktrace) and returns a stable JSON error payload.</li>
 * </ul>
 *
 * <p>Security note:
 * This includes the exception class and message to speed up debugging in preview environments.
 * If later needed, this can be gated by an environment property.
 */
@Component
@Order(-2) // Run before the default Spring Boot WebFlux error handler
public class GatewayErrorHandler implements ErrorWebExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GatewayErrorHandler.class);

  private final ObjectMapper objectMapper;

  public GatewayErrorHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (exchange.getResponse().isCommitted()) {
      return Mono.error(ex);
    }

    HttpStatus status = mapStatus(ex);

    String requestId = exchange.getRequest().getId();
    String method = exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "UNKNOWN";
    String path = exchange.getRequest().getURI().getPath();

    // Targeted logging: this is the most important line for pinpointing the real failure cause.
    log.error(
        "Gateway.proxyError requestId={} method={} path={} status={} exType={} msg={}",
        requestId,
        method,
        path,
        status.value(),
        ex.getClass().getName(),
        ex.getMessage(),
        ex
    );

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("path", path);
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("requestId", requestId);

    // Surface the root cause to callers (useful in preview/CI; can be restricted later).
    body.put("exception", ex.getClass().getName());
    body.put("message", safeMessage(ex.getMessage()));

    byte[] bytes;
    try {
      bytes = objectMapper.writeValueAsBytes(body);
    } catch (JsonProcessingException jsonEx) {
      // Extremely defensive fallback; should not happen in practice.
      String fallback = "{\"status\":" + status.value() + ",\"error\":\"" + status.getReasonPhrase() + "\"}";
      bytes = fallback.getBytes(StandardCharsets.UTF_8);
    }

    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
  }

  private static String safeMessage(String msg) {
    if (msg == null) {
      return null;
    }
    // Prevent extremely large messages from ballooning gateway responses.
    int max = 500;
    return msg.length() <= max ? msg : msg.substring(0, max) + "...";
  }

  private static HttpStatus mapStatus(Throwable ex) {
    // Connectivity problems should NOT be opaque 500s. Use 503 to make it clear it's a dependency issue.
    if (hasCause(ex, UnknownHostException.class)
        || hasCause(ex, ConnectException.class)
        || hasCause(ex, SocketTimeoutException.class)) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
    // Default: internal gateway error.
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
    Throwable cur = ex;
    while (cur != null) {
      if (type.isInstance(cur)) {
        return true;
      }
      cur = cur.getCause();
    }
    return false;
  }
}
