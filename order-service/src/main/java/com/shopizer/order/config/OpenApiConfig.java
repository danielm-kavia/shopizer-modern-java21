package com.shopizer.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for order-service.
 */
@Configuration
public class OpenApiConfig {

  // PUBLIC_INTERFACE
  @Bean
  public OpenAPI orderOpenApi() {
    /** Defines OpenAPI metadata and tags for order-service. */
    return new OpenAPI()
        .info(new Info()
            .title("Shopizer Order Service API")
            .description("Order domain APIs (create orders, retrieve order details, list orders).")
            .version("0.1.0-SNAPSHOT"))
        .addTagsItem(new Tag().name("Orders").description("Order endpoints (create and query orders)"))
        .addTagsItem(new Tag().name("Health").description("Service health endpoints"));
  }
}
