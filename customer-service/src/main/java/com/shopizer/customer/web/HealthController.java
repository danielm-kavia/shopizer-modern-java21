package com.shopizer.customer.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal health endpoint for local verification.
 */
@RestController
public class HealthController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("status", "UP", "service", "customer-service");
  }
}
