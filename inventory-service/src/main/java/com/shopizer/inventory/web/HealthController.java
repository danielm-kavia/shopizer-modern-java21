package com.shopizer.inventory.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health")
public class HealthController {

  // PUBLIC_INTERFACE
  @GetMapping("/health")
  @Operation(summary = "Health check", description = "Simple health endpoint for local/dev usage and k8s probes.")
  public String health() {
    /** Returns service health status. */
    return "OK";
  }
}
