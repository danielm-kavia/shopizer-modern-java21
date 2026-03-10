package com.shopizer.pricing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for pricing-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI pricingOpenApi() {
    /** Defines OpenAPI metadata and tags for pricing-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Pricing Service API")
            .description("Read-only pricing APIs for resolving effective prices (regular vs sale) per store/SKU/currency.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Pricing").description("Pricing endpoints (resolve effective price)"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
