package com.shopizer.gateway.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public landing endpoint for preview environments.
 *
 * <p>The gateway is primarily an authenticated edge (JWT via Keycloak), but most preview systems
 * expect {@code GET /} to return a non-401 response. This controller provides a simple public
 * response and points users to the health endpoint and Swagger/OpenAPI docs.
 */
@RestController
public class LandingController {

  // PUBLIC_INTERFACE
  @GetMapping("/")
  public Map<String, Object> landing() {
    /** Returns a public landing payload to confirm the service is reachable without auth. */
    return Map.of(
        "service", "shopizer-gateway",
        "status", "OK",
        "health", "/health",
        "openapi", "/openapi.json");
  }
}
