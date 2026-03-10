package com.shopizer.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for catalog-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI catalogOpenApi() {
    /** Defines OpenAPI metadata and tags for catalog-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Catalog Service API")
            .description("Catalog domain APIs (products, categories) for Shopizer modernization.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Catalog").description("Catalog endpoints (products, categories)"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
