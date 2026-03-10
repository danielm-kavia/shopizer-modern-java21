package com.shopizer.cart.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health endpoint for cart-service.
 */
@RestController
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  public Map<String, Object> health() {
    /** Simple health response for environments where actuator is not used. */
    return Map.of("status", "UP", "service", "cart-service");
  }
}
