package com.shopizer.gateway.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal controller to verify the gateway is running.
 */
@RestController
public class HealthController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of("status", "UP", "service", "gateway");
  }
}
