package com.shopizer.checkout.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health endpoint (in addition to /actuator/health).
 */
@RestController
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  public String health() {
    /** Lightweight health check. */
    return "OK";
  }
}
