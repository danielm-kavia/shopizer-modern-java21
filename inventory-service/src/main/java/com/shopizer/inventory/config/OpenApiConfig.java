package com.shopizer.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for inventory-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI inventoryOpenApi() {
    /** Defines OpenAPI metadata and tags for inventory-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Inventory Service API")
            .description("Inventory APIs for stock availability and synchronous reservation (on_hand/reserved) per store and SKU.")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Inventory").description("Inventory endpoints (stock query/reservation)"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
