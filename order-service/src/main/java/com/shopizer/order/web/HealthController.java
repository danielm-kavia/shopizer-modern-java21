package com.shopizer.order.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health endpoint (separate from Actuator) for parity with other services.
 */
@RestController
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  public Map<String, String> health() {
    /** Returns a minimal health payload. */
    return Map.of("status", "UP", "service", "order-service");
  }
}
