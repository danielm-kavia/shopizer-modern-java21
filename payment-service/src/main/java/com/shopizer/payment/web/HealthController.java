package com.shopizer.payment.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health endpoint (in addition to /actuator/health).
 */
@RestController
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  public Map<String, Object> health() {
    /** Returns basic liveness information. */
    return Map.of("status", "UP", "service", "payment-service");
  }
}
