package com.shopizer.tax.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for tax-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI taxOpenApi() {
    /** Defines OpenAPI metadata and tags for tax-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Tax Service API")
            .description("Tax APIs for calculating taxes (Phase 1: simple store-level percentage rate).")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Tax").description("Tax calculation endpoints"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
