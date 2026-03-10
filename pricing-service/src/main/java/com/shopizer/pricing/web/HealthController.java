package com.shopizer.pricing.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight health endpoint (in addition to Spring Actuator).
 */
@RestController
@Tag(name = "Health")
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  @Operation(
      summary = "Health check",
      description = "Simple health endpoint for container/orchestrator checks."
  )
  public Map<String, Object> health() {
    /** Returns a minimal health payload. */
    return Map.of("status", "UP", "service", "pricing-service");
  }
}
