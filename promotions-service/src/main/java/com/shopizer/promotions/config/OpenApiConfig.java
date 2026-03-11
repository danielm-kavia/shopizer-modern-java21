package com.shopizer.promotions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for promotions-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI promotionsOpenApi() {
    /** Defines OpenAPI metadata and tags for promotions-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Promotions Service API")
            .description("Read-only coupon apply APIs backed by Flyway+JPA coupon definitions.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Promotions").description("Coupon apply endpoints"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
