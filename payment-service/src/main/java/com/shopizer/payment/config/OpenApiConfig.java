package com.shopizer.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for payment-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI paymentOpenApi() {
    /** Defines OpenAPI metadata and tags for payment-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Payment Service API")
            .description("Authorize-only payment APIs (Phase 1). Provider: PayPal.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Payments").description("Authorize-only payment endpoints"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
