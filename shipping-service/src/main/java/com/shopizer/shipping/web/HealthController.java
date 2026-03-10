package com.shopizer.shipping.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic health endpoint (in addition to actuator).
 */
@RestController
@Tag(name = "Health", description = "Service health endpoints")
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Returns a simple health payload for the service.")
  public Map<String, Object> health() {
    /** Returns health status. */
    return Map.of("status", "UP", "service", "shipping-service");
  }
}
