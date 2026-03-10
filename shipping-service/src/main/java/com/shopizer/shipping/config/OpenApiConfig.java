package com.shopizer.shipping.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for shipping-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI shippingOpenApi() {
    /** Defines OpenAPI metadata and tags for shipping-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Shipping Service API")
            .description("Shipping rate quote APIs (Phase 1: stub providers only).")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Shipping").description("Shipping rate quote endpoints"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
