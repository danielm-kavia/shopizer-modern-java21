package com.shopizer.promotions.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health endpoint for local dev and demos.
 */
@RestController
@Tag(name = "Health", description = "Service health endpoints")
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Returns a simple OK payload if the service is running.")
  public Map<String, Object> health() {
    /** Health endpoint returning a simple status payload. */
    return Map.of("status", "ok", "service", "promotions-service");
  }
}
