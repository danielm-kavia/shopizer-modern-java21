package com.shopizer.tax.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lightweight health endpoint for environments that don't expose actuator.
 */
@RestController
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  public String health() {
    /** Returns a minimal health string to verify the service is up. */
    return "ok";
  }
}
