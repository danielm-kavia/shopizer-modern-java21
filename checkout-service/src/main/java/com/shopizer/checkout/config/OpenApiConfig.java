package com.shopizer.checkout.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for checkout-service.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI checkoutOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Checkout Service API")
            .description("Checkout orchestration API (Phase 1: create order from cart items).")
            .version("0.1.0"));
  }
}
