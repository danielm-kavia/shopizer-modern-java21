package com.shopizer.customer.web;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal customer endpoint to validate JWT auth and gateway routing.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
    return Map.of(
        "sub", jwt.getSubject(),
        "preferred_username", jwt.getClaimAsString("preferred_username"),
        "email", jwt.getClaimAsString("email"),
        "issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null
    );
  }
}
