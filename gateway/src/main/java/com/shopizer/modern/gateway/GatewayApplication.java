package com.shopizer.modern.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Cloud Gateway application for shopizer-modern-java21 previews.
 *
 * It binds to server.port (default 3001) and exposes Actuator health endpoints
 * for readiness checks.
 */
@SpringBootApplication
public class GatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(GatewayApplication.class, args);
  }
}
